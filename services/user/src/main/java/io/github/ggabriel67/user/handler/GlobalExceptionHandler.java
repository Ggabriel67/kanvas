package io.github.ggabriel67.user.handler;

import io.github.ggabriel67.user.exception.CredentialAlreadyTakenException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashSet;
import java.util.Set;

@RestControllerAdvice
public class GlobalExceptionHandler
{
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ExceptionResponse> handleException(BadCredentialsException exception) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(
                        ExceptionResponse.builder()
                                .errorCode(ErrorCode.BAD_CREDENTIALS.getCode())
                                .errorDescription(ErrorCode.BAD_CREDENTIALS.getDescription())
                                .error(ErrorCode.BAD_CREDENTIALS.getDescription())
                                .build()
                );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ExceptionResponse> handleException(MethodArgumentNotValidException exception) {
        Set<String> errors = new HashSet<>();
        exception.getBindingResult().getAllErrors()
                .forEach(error -> errors.add(error.getDefaultMessage()));
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(
                        ExceptionResponse.builder()
                                .validationErrors(errors)
                                .build()
                );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionResponse> handleException(Exception exception) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(
                        ExceptionResponse.builder()
                                .errorDescription("Internal server error")
                                .error(exception.getMessage())
                                .build()
                );
    }

    @ExceptionHandler(CredentialAlreadyTakenException.class)
    public ResponseEntity<ExceptionResponse> handleException(CredentialAlreadyTakenException exception) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(
                        ExceptionResponse.builder()
                                .error(exception.getMessage())
                                .build()
                );
    }
}
