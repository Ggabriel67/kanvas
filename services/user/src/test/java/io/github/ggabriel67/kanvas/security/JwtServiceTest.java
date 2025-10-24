package io.github.ggabriel67.kanvas.security;

import io.github.ggabriel67.kanvas.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JwtService Unit Tests")
class JwtServiceTest
{
    private JwtService jwtService;
    private UserDetails user;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(1000 * 60, 1000 * 120);
        jwtService.setSecretKey("acd57fe63f11c84ca7ba1da03d3a99bceb7427ccbb6103488397a84c04f2894b");

        user = User.builder()
                .email("useremail@email.com")
                .password("password")
                .build();
    }

    @Test
    void generateAndValidateAccessToken() {
        String token = jwtService.generateAccessToken(Map.of(), user);

        assertThat(token).isNotNull();
        assertThat(jwtService.extractUsername(token)).isEqualTo("useremail@email.com");
        assertThat(jwtService.isTokenValid(token, user)).isTrue();
    }

    @Test
    void generateAndValidateRefreshToken() {
        String token = jwtService.generateRefreshToken(user);

        assertThat(token).isNotNull();
        assertThat(jwtService.isTokenValid(token, user)).isTrue();
    }

    @Test
    void tokenShouldBeInvalid_WhenDifferentUser() {
        String token = jwtService.generateAccessToken(Map.of(), user);

        UserDetails otherUser = User.builder()
                .email("otheremail@email.com")
                .password("password")
                .build();

        assertThat(jwtService.isTokenValid(token, otherUser)).isFalse();
    }

    @Test
    void shouldExtractExtraClaimFromAccessToken() {
        String token = jwtService.generateAccessToken(Map.of("userId", 1), user);

        assertThat(jwtService.extractClaim(token, claims ->
                claims.get("userId", Integer.class)).equals(1));
    }
}