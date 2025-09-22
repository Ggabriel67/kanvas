package io.github.ggabriel67.kanvas.gateway.security;

import io.github.ggabriel67.kanvas.gateway.routing.RouteValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@RefreshScope
public class AuthGatewayFilter implements GlobalFilter
{
    private final JwtService jwtService;
    private final RouteValidator routeValidator;
    private final WebClient.Builder webClientBuilder;

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
            return onError(exchange, "No access token", HttpStatus.UNAUTHORIZED);
        }

        if (!jwtService.isTokenValid(accessToken)) {
            return onError(exchange, "Invalid access token", HttpStatus.UNAUTHORIZED);
        }

        Route route = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
        String service = route.getUri().getHost();
        Integer userId = jwtService.extractUserId(accessToken);
        if ("BOARD-SERVICE".equals(service)) {
            ServerWebExchange mutated = exchange.mutate()
                    .request(
                            r -> r.headers(
                                    h -> h.add("X-User-Id", String.valueOf(userId))))
                    .build();
            return chain.filter(mutated);
        }
        else if ("TASK-SERVICE".equals(service)) {
            String boardId = request.getHeaders().getFirst("X-Board-Id");
            return webClientBuilder.build()
                    .get()
                    .uri("http://board-service/api/v1/boards/{boardId}/roles", boardId)
                    .header("X-User-Id", String.valueOf(userId))
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, response -> Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN, "User has no role in this board")))
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
