package io.github.ggabriel67.user.auth;

import io.github.ggabriel67.user.exception.CredentialAlreadyTakenException;
import io.github.ggabriel67.user.security.JwtService;
import io.github.ggabriel67.user.token.Token;
import io.github.ggabriel67.user.token.TokenRepository;
import io.github.ggabriel67.user.token.TokenType;
import io.github.ggabriel67.user.user.User;
import io.github.ggabriel67.user.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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

    public void register(RegistrationRequest request) throws RuntimeException{

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
                .build();
        userRepository.save(user);
    }

    public String authenticate(AuthenticationRequest request) {
        var auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(), request.password()
                )
        );
        var user = ((User)auth.getPrincipal());

        String jwtToken = jwtService.generateToken(user);
        revokeAllUserTokens(user);
        saveUserToken(user, jwtToken);

        return jwtToken;
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

    private void saveUserToken(User user, String jwtToken) {
        var token = Token.builder()
                .user(user)
                .token(jwtToken)
                .tokenType(TokenType.BEARER)
                .revoked(false)
                .expired(false)
                .build();
        tokenRepository.save(token);
    }
}
