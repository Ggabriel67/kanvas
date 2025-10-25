package io.github.ggabriel67.kanvas;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.ggabriel67.kanvas.auth.AuthenticationRequest;
import io.github.ggabriel67.kanvas.auth.AuthenticationResponse;
import io.github.ggabriel67.kanvas.auth.RegistrationRequest;
import io.github.ggabriel67.kanvas.kafka.UserEventProducer;
import io.github.ggabriel67.kanvas.token.TokenRepository;
import io.github.ggabriel67.kanvas.token.TokenType;
import io.github.ggabriel67.kanvas.user.User;
import io.github.ggabriel67.kanvas.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class UserServiceIntegrationTest
{
    @MockitoBean
    private UserEventProducer userEventProducer;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private TokenRepository tokenRepository;

    @BeforeEach
    void cleanDatabase() {
        tokenRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void register_ShouldRegisterUserSuccessfully() throws Exception {
        RegistrationRequest request = new RegistrationRequest(
                "John", "Doe", "johndoe@email.com", "johndoe", "password"
        );

            mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        var savedUser = userRepository.findByEmail("johndoe@email.com").orElseThrow();
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getDisplayUsername()).isEqualTo("johndoe");
        assertThat(savedUser.getPassword()).isNotEqualTo("password");
    }

    @Test
    void register_ShouldReturnBadRequestWhenEmailAlreadyExists() throws Exception {
        userRepository.save(User.builder().firstname("Jane").lastname("Doe").email("doe@email.com").username("janedoe").password("hash").build());

        RegistrationRequest request = new RegistrationRequest(
                "John", "Doe", "doe@email.com", "johndoe", "password"
        );

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void authenticate_ShouldAuthenticateUserAndReturnCookies() throws Exception {
        var user = User.builder().firstname("John").lastname("Doe").email("john@example.com")
                .username("johndoe").password(passwordEncoder.encode("password123")).build();
        userRepository.save(user);

        AuthenticationRequest request = new AuthenticationRequest("john@example.com", "password123");

        var result = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(cookie().exists(TokenType.ACCESS.getName()))
                .andExpect(cookie().exists(TokenType.REFRESH.getName()))
                .andReturn();

        var responseBody = result.getResponse().getContentAsString();
        var authResponse = objectMapper.readValue(responseBody, AuthenticationResponse.class);

        assertThat(authResponse.accessToken()).isNotBlank();
    }

    @Test
    void authenticate_ShouldRejectWhenInvalidCredentials() throws Exception {
        var user = User.builder().firstname("John").lastname("Doe").email("john@example.com")
                .username("johndoe").password(passwordEncoder.encode("password123")).build();
        userRepository.save(user);

        var request = new AuthenticationRequest("john@example.com", "wrongpassword");

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
}
