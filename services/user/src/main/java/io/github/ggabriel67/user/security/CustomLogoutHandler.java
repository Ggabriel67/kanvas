package io.github.ggabriel67.user.security;

import io.github.ggabriel67.user.token.Token;
import io.github.ggabriel67.user.token.TokenRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomLogoutHandler implements LogoutHandler
{
    private final TokenRepository tokenRepository;

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {

        String jwtToken = null;

        for (Cookie cookie : request.getCookies()) {
            if (cookie.getName().equals("jwt")) {
                jwtToken = cookie.getValue();
                break;
            }
        }

        Token storedToken = tokenRepository.findByToken(jwtToken)
                .orElse(null);
        if (storedToken != null) {
            storedToken.setExpired(true);
            storedToken.setRevoked(true);
            tokenRepository.save(storedToken);
        }
    }
}
