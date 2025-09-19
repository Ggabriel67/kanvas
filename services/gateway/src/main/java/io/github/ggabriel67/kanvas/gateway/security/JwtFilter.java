package io.github.ggabriel67.kanvas.gateway.security;

import io.github.ggabriel67.kanvas.gateway.routing.RouteValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@RefreshScope
public class JwtFilter implements GlobalFilter
{
    private final JwtService jwtService;
    private final RouteValidator routeValidator;
    private final WebClient webClient;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        if (!routeValidator.isSecured.test(request)) return chain.filter(exchange);

        String accessToken = null;

        for (var entry : request.getCookies().entrySet()) {
            for (var cookie : entry.getValue()) {
                if (cookie.getName().equals("accessToken")) {
                    accessToken = cookie.getValue();
                    break;
                }
            }
        }

        if (accessToken == null) {
            return onError(exchange, "No token", HttpStatus.UNAUTHORIZED);
        }

        if (!jwtService.isTokenValid(accessToken)) {
            return onError(exchange, "Invalid token", HttpStatus.UNAUTHORIZED);
        }

        String route = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_PREDICATE_MATCHED_PATH_ATTR);
        System.out.println("Route: " + route);

        Integer userId = jwtService.extractUserId(accessToken);
        System.out.println("User id: " + userId);
        if ("/board-service/**".equals(route)) {
            ServerWebExchange mutated = exchange.mutate()
                    .request(
                            r -> r.headers(
                                    h -> h.add("X-User-Id", String.valueOf(userId))))
                    .build();
            return chain.filter(mutated);
        }
        else if ("/task-service/**".equals(route)) {
            String boardId = request.getHeaders().getFirst("X-Board-Role");
            webClient.get()
                    .uri("http://localhost:8222/api/v1/boards/{boardId}/roles", boardId)
                    .header("X-User-Id", String.valueOf(userId))
                    .retrieve()
                    .bodyToMono(String.class)
                    .flatMap(boardRole -> {
                        ServerWebExchange mutated = exchange.mutate()
                                .request(r -> r.headers(h -> h.add("X-Board-Role", boardRole)))
                                .build();
                        return chain.filter(mutated);
                    });
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
