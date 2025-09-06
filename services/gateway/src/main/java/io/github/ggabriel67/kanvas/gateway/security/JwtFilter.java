package io.github.ggabriel67.kanvas.gateway.security;

import io.github.ggabriel67.kanvas.gateway.routing.RouteValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@RefreshScope
public class JwtFilter implements GlobalFilter
{
    private final JwtService jwtService;
    private final RouteValidator routeValidator;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String accessToken = null;
        ServerHttpRequest request = exchange.getRequest();

        System.out.println("inside the filter");

        if (routeValidator.isSecured.test(request)) {
            System.out.println("Protected route");

            for (var entry : request.getCookies().entrySet()) {
                for (var cookie : entry.getValue()) {
                    if (cookie.getName().equals("accessToken")) {
                        accessToken = cookie.getValue();
                        break;
                    }
                }
            }

            if (accessToken == null) {
                return chain.filter(exchange);
            }

            if (isCredentialMissing(accessToken)) {
                System.out.println("credential missing");
                return onError(exchange, "Credentials missing", HttpStatus.UNAUTHORIZED);
            }

            if (!jwtService.isTokenValid(accessToken)) {
                return onError(exchange, "Invalid token", HttpStatus.UNAUTHORIZED);
            }
        }

        return chain.filter(exchange);
    }

    private Mono<Void> onError(ServerWebExchange exchange, String error, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        return response.setComplete();
    }

    private boolean isCredentialMissing(String token) {
        return jwtService.extractUsername(token).isEmpty();
    }
}
