package io.github.ggabriel67.kanvas.user;

public record UserDto(
        Integer id,
        String firstname,
        String lastname,
        String email,
        String username,
        String avatarColor
) {
}

