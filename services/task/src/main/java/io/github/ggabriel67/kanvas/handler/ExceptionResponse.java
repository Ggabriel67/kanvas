package io.github.ggabriel67.kanvas.handler;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Set;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record ExceptionResponse(
        Set<String> validationErrors
) {
}