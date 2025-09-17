package io.github.ggabriel67.kanvas.event.user;

public record UserCreated(
        Integer id,
        String firstname,
        String lastname,
        String email,
        String username,
        String avatarColor
) {
}
