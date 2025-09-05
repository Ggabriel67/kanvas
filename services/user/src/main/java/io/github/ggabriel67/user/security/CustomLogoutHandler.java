package io.github.ggabriel67.user.security;

import io.github.ggabriel67.user.token.Token;
import io.github.ggabriel67.user.token.TokenRepository;
import io.github.ggabriel67.user.token.TokenType;
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

        String accessToken = null;

        for (Cookie cookie : request.getCookies()) {
            if (cookie.getName().equals(TokenType.ACCESS.getName())) {
                accessToken = cookie.getValue();
                break;
            }
        }

        Token storedToken = tokenRepository.findByToken(accessToken)
                .orElse(null);
        if (storedToken != null) {
            storedToken.setExpired(true);
            storedToken.setRevoked(true);
            tokenRepository.save(storedToken);
        }
    }
}
