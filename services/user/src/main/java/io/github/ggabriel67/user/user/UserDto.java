package io.github.ggabriel67.user.user;

public record UserDto(
        Integer id,
        String firstname,
        String lastname,
        String email,
        String username,
        String avatarColor
) {
}
