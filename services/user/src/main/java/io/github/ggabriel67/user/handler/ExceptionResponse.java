package io.github.ggabriel67.user.handler;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.Map;
import java.util.Set;

@Builder
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record ExceptionResponse(
        Integer errorCode,
        String errorDescription,
        String error,
        Set<String> validationErrors,
        Map<String, String> errors
) {

}
