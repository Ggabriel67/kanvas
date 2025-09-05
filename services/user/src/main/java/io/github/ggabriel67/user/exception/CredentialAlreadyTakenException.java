package io.github.ggabriel67.user.exception;

public class CredentialAlreadyTakenException extends RuntimeException {
    public CredentialAlreadyTakenException(String message) {
        super(message);
    }
}
