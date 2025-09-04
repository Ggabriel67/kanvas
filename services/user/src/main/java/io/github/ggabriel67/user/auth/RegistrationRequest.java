package io.github.ggabriel67.user.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;

public record RegistrationRequest(
        @NotEmpty(message = "Firstname is mandatory")
        @NotBlank(message = "Firstname is mandatory")
        String firstname,
        @NotEmpty(message = "Lastname is mandatory")
        @NotBlank(message = "Lastname is mandatory")
        String lastname,
        @Email(message = "Incorrect email")
        @NotEmpty(message = "Email is mandatory")
        @NotBlank(message = "Email is mandatory")
        String email,
        @NotEmpty(message = "Username is mandatory")
        @NotBlank(message = "Username is mandatory")
        String username,
        @NotEmpty(message = "Password is mandatory")
        @NotBlank(message = "Password is mandatory")
        @Size(min = 8, message = "Password should be at least 8 characters long")
        String password
) {
}
