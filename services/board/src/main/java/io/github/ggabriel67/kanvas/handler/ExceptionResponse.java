package io.github.ggabriel67.kanvas.handler;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

import java.util.Set;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record ExceptionResponse(
        Set<String> validationErrors
) {
}
