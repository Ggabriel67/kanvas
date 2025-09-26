package io.github.ggabriel67.kanvas.auth;

import io.github.ggabriel67.kanvas.event.user.UserCreated;
import io.github.ggabriel67.kanvas.exception.CredentialAlreadyTakenException;
import io.github.ggabriel67.kanvas.kafka.UserEventProducer;
import io.github.ggabriel67.kanvas.security.JwtService;
import io.github.ggabriel67.kanvas.token.Token;
import io.github.ggabriel67.kanvas.token.TokenRepository;
import io.github.ggabriel67.kanvas.token.TokenType;
import io.github.ggabriel67.kanvas.user.User;
import io.github.ggabriel67.kanvas.user.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.DelegatingServletOutputStream;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.ByteArrayOutputStream;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthenticationService Unit Tests")
class AuthenticationServiceTest
{
    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private JwtService jwtService;
    @Mock private TokenRepository tokenRepository;
    @Mock private UserEventProducer userEventProducer;
    @Mock private HttpServletResponse httpServletResponse;

    @InjectMocks
    private AuthenticationService authenticationService;

    private RegistrationRequest registrationRequest;

    @BeforeEach()
    void setUp() {
        registrationRequest = new RegistrationRequest("John", "Doe", "johndoe@email.com", "johndoe", "password123");
    }

    @Test
    void register_ShouldThrowException_WhenEmailAlreadyTaken() {
        when(userRepository.findByEmail("johndoe@email.com"))
                .thenReturn(Optional.of(new User()));

        assertThatThrownBy(() -> authenticationService.register(registrationRequest))
                .isInstanceOf(CredentialAlreadyTakenException.class)
                .hasMessageContaining("This email is already taken");

        verify(userRepository, never()).save(any());
        verify(userEventProducer, never()).sendUserCreated(any());
    }

    @Test
    void register_ShouldThrowException_WhenUsernameAlreadyTaken() {
        when(userRepository.findByEmail("johndoe@email.com"))
                .thenReturn(Optional.empty());
        when(userRepository.findByUsername("johndoe"))
                .thenReturn(Optional.of(new User()));

        assertThatThrownBy(() -> authenticationService.register(registrationRequest))
                .isInstanceOf(CredentialAlreadyTakenException.class)
                .hasMessageContaining("This username is already taken");

        verify(userRepository, never()).save(any());
        verify(userEventProducer, never()).sendUserCreated(any());
    }

    @Test
    void register_ShouldSaveUserAndSendKafkaEvent_WhenValidRequest() {
        when(userRepository.findByEmail("johndoe@email.com")).thenReturn(Optional.empty());
        when(userRepository.findByUsername("johndoe")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password123")).thenReturn("hashedPassword");

        authenticationService.register(registrationRequest);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        verify(userEventProducer).sendUserCreated(any(UserCreated.class));

        User savedUser = userCaptor.getValue();
        assert savedUser.getPassword().equals("hashedPassword");
        assert savedUser.getEmail().equals("johndoe@email.com");
    }

    @Test
    void authenticate_ShouldReturnTokenAndAddCookies() {
        AuthenticationRequest authRequest = new AuthenticationRequest("johndoe@email.com", "password123");

        User user = User.builder()
                .id(1)
                .email("johndoe@email.com")
                .username("johndoe")
                .password("hashedPassword")
                .build();

        var authentication = new UsernamePasswordAuthenticationToken(user, null);

        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(jwtService.generateAccessToken(anyMap(), eq(user))).thenReturn("accessToken");
        when(jwtService.generateRefreshToken(user)).thenReturn("refreshToken");

        AuthenticationResponse response = authenticationService.authenticate(authRequest, httpServletResponse);

        assert response.accessToken().equals("accessToken");
        verify(tokenRepository).save(any(Token.class));
        verify(httpServletResponse, times(2)).addCookie(any(Cookie.class));
    }

    @Test
    void authenticate_ShouldThrowException_WhenBadCredentials() {
        AuthenticationRequest authRequest = new AuthenticationRequest("johndoe@email.com", "wrongpassword");

        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> authenticationService.authenticate(authRequest, httpServletResponse))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("Bad credentials");

        verify(jwtService, never()).generateAccessToken(anyMap(), any());
        verify(tokenRepository, never()).save(any());
    }

    @Test
    void refreshToken_ShouldGenerateNewAccessToken_WhenRefreshTokenValid() throws Exception {
        Cookie refreshCookie = new Cookie(TokenType.REFRESH.getName(), "validRefreshToken");
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        when(mockRequest.getCookies()).thenReturn(new Cookie[]{refreshCookie});

        HttpServletResponse mockResponse = mock(HttpServletResponse.class);
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        when(mockResponse.getOutputStream()).thenReturn(new DelegatingServletOutputStream(outContent));

        when(jwtService.extractUsername("validRefreshToken")).thenReturn("johndoe@email.com");
        User user = User.builder().id(1).email("johndoe@email.com").build();
        when(userRepository.findByEmail("johndoe@email.com")).thenReturn(Optional.of(user));
        when(tokenRepository.findByToken("validRefreshToken"))
                .thenReturn(Optional.of(Token.builder().revoked(false).expired(false).build()));
        when(jwtService.isTokenValid("validRefreshToken", user)).thenReturn(true);
        when(jwtService.generateAccessToken(anyMap(), eq(user))).thenReturn("newAccessToken");
        when(jwtService.getAccessTokenExpiration()).thenReturn(60000L);
        when(jwtService.getRefreshTokenExpiration()).thenReturn(120000L);

        authenticationService.refreshToken(mockRequest, mockResponse);

        verify(mockResponse, atLeastOnce()).addCookie(any(Cookie.class));

        String jsonResponse = outContent.toString();
        assertThat(jsonResponse).contains("newAccessToken");
    }

    @Test
    void refreshToken_ShouldNotGenerateNewAccessToken_WhenRefreshTokenAbsent() throws Exception {
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        when(mockRequest.getCookies()).thenReturn(new Cookie[]{});

        MockHttpServletResponse response = new MockHttpServletResponse();

        authenticationService.refreshToken(mockRequest, response);

        verify(jwtService, never()).extractUsername(any());
        verify(userRepository, never()).findByEmail(any());
        verify(tokenRepository, never()).findByToken(any());
        verify(jwtService, never()).isTokenValid(any(), any());
        verify(jwtService, never()).generateAccessToken(any(), any());

        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_UNAUTHORIZED);
        assertThat(response.getCookies()).isEmpty();
        assertThat(response.getContentAsString()).isEmpty();
    }

    @Test
    void refreshToken_ShouldNotGenerateNewAccessToken_WhenRefreshTokenInvalid() throws Exception {
        Cookie refreshCookie = new Cookie(TokenType.REFRESH.getName(), "invalidRefreshToken");
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(mockRequest.getCookies()).thenReturn(new Cookie[]{refreshCookie});
        when(jwtService.extractUsername("invalidRefreshToken")).thenReturn("johndoe@email.com");
        User user = User.builder().id(1).email("johndoe@email.com").build();
        when(userRepository.findByEmail("johndoe@email.com")).thenReturn(Optional.of(user));
        when(tokenRepository.findByToken("invalidRefreshToken"))
                .thenReturn(Optional.of(Token.builder().revoked(true).expired(true).build()));
        when(jwtService.isTokenValid("invalidRefreshToken", user)).thenReturn(false);

        authenticationService.refreshToken(mockRequest, response);

        verify(jwtService, never()).generateAccessToken(any(), any());

        assertThat(response.getCookies()).isEmpty();
        assertThat(response.getContentAsString()).isEmpty();
    }
}
