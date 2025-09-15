package io.github.ggabriel67.kanvas.security;

import io.github.ggabriel67.kanvas.auth.AuthenticationService;
import io.github.ggabriel67.kanvas.token.Token;
import io.github.ggabriel67.kanvas.token.TokenRepository;
import io.github.ggabriel67.kanvas.token.TokenType;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomLogoutHandler implements LogoutHandler
{
    private final TokenRepository tokenRepository;
    private final AuthenticationService authenticationService;

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        String refreshToken = null;

        for (Cookie cookie : request.getCookies()) {
            if (cookie.getName().equals(TokenType.REFRESH.getName())) {
                refreshToken = cookie.getValue();
                break;
            }
        }

        Token storedToken = tokenRepository.findByToken(refreshToken)
                .orElse(null);
        if (storedToken != null) {
            storedToken.setExpired(true);
            storedToken.setRevoked(true);
            tokenRepository.save(storedToken);
        }

        response.addCookie(deleteCookie(TokenType.ACCESS.getName()));
        response.addCookie(deleteCookie(TokenType.REFRESH.getName()));
    }

    private Cookie deleteCookie(String name) {
        Cookie cookie = new Cookie(name, "");
        cookie.setPath("/");
        cookie.setMaxAge(0);
        cookie.setHttpOnly(true);
        cookie.setAttribute("SameSite", "Strict");
        return cookie;
    }
}
