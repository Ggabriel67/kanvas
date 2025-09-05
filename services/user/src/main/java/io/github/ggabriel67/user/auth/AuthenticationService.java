package io.github.ggabriel67.user.auth;

import io.github.ggabriel67.user.exception.CredentialAlreadyTakenException;
import io.github.ggabriel67.user.security.JwtService;
import io.github.ggabriel67.user.user.User;
import io.github.ggabriel67.user.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService
{
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public void register(RegistrationRequest request) throws RuntimeException{

        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new CredentialAlreadyTakenException("This email is already taken");
        }

        if (userRepository.findByUsername(request.username()).isPresent()){
            throw new CredentialAlreadyTakenException("This username is already taken");
        }

        User user = User.builder()
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
        return jwtService.generateToken(user);
    }
}
