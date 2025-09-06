package io.github.ggabriel67.kanvas.gateway.routing;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.AntPathMatcher;

import java.util.List;
import java.util.function.Predicate;

@Service
public class RouteValidator
{
    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    public static final List<String> unprotectedUrls = List.of("/api/v1/auth/**");

    public Predicate<ServerHttpRequest> isSecured = request ->
            unprotectedUrls.stream().noneMatch(uri -> pathMatcher.match(uri, request.getURI().getPath()));
}
