package io.github.ggabriel67.kanvas.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.ggabriel67.kanvas.exception.CredentialAlreadyTakenException;
import io.github.ggabriel67.kanvas.kafka.UserProducer;
import io.github.ggabriel67.kanvas.security.JwtService;
import io.github.ggabriel67.kanvas.user.AvatarColorGenerator;
import io.github.ggabriel67.kanvas.user.User;
import io.github.ggabriel67.kanvas.token.Token;
import io.github.ggabriel67.kanvas.token.TokenRepository;
import io.github.ggabriel67.kanvas.token.TokenType;
import io.github.ggabriel67.kanvas.user.UserMapper;
import io.github.ggabriel67.kanvas.user.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthenticationService
{
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final TokenRepository tokenRepository;
    private final UserProducer userProducer;
    private final UserMapper userMapper;

    public void register(RegistrationRequest request) throws RuntimeException {

        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new CredentialAlreadyTakenException("This email is already taken");
        }

        if (userRepository.findByUsername(request.username()).isPresent()){
            throw new CredentialAlreadyTakenException("This username is already taken");
        }

        var user = User.builder()
                .firstname(request.firstname())
                .lastname(request.lastname())
                .email(request.email())
                .username(request.username())
                .password(passwordEncoder.encode(request.password()))
                .avatarColor(AvatarColorGenerator.generateColor(request.username()))
                .build();
        userRepository.save(user);
        userProducer.sendUserReplica(userMapper.toUserDto(user));
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request, HttpServletResponse response) {
        var auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(), request.password()
                )
        );
        var user = ((User)auth.getPrincipal());

        String accessToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        revokeAllUserTokens(user);
        saveUserToken(user, accessToken);

        Cookie accessTokenCookie = generateCookie(TokenType.ACCESS, accessToken, jwtService.getAccessTokenExpiration());
        Cookie refreshTokenCookie = generateCookie(TokenType.REFRESH, refreshToken, jwtService.getRefreshTokenExpiration());
        response.addCookie(accessTokenCookie);
        response.addCookie(refreshTokenCookie);

        return AuthenticationResponse.builder()
                .accessToken(accessToken)
                .build();
    }

    private void revokeAllUserTokens(User user) {
        List<Token> validUserTokens = tokenRepository.findAllValidTokensByUser(user.getId());
        if (validUserTokens.isEmpty())
            return;
        validUserTokens.forEach(token -> {
            token.setRevoked(true);
            token.setExpired(true);
        });
        tokenRepository.saveAll(validUserTokens);
    }

    private void saveUserToken(User user, String accessToken) {
        var token = Token.builder()
                .user(user)
                .token(accessToken)
                .tokenType(TokenType.ACCESS)
                .revoked(false)
                .expired(false)
                .build();
        tokenRepository.save(token);
    }

    public void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
        final String userEmail;
        String refreshToken = null;

        for (Cookie cookie : request.getCookies()) {
            if (cookie.getName().equals(TokenType.REFRESH.getName())) {
                refreshToken = cookie.getValue();
                break;
            }
        }

        if (refreshToken == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        userEmail = jwtService.extractUsername(refreshToken);
        if (userEmail != null) {
            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow();

            if (jwtService.isTokenValid(refreshToken, user)) {
                String accessToken = jwtService.generateToken(user);
                revokeAllUserTokens(user);
                saveUserToken(user, accessToken);
                AuthenticationResponse authResponse = AuthenticationResponse.builder()
                        .accessToken(accessToken)
                        .build();

                Cookie accessTokenCookie = generateCookie(TokenType.ACCESS, accessToken, jwtService.getAccessTokenExpiration());
                Cookie refreshTokenCookie = generateCookie(TokenType.REFRESH, refreshToken, jwtService.getRefreshTokenExpiration());
                response.addCookie(accessTokenCookie);
                response.addCookie(refreshTokenCookie);
                new ObjectMapper().writeValue(response.getOutputStream(), authResponse);
            }
        }
    }

    private Cookie generateCookie(TokenType tokenType, String token, long expiration) {
        Cookie cookie = new Cookie(tokenType.getName(), token);
        cookie.setMaxAge((int) expiration / 1000);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setAttribute("SameSite", "Strict");
        return cookie;
    }
}
