package io.github.ggabriel67.kanvas.board.user;

import org.springframework.stereotype.Service;

@Service
public class UserMapper
{
    public UserDto toUserDto(User user) {
        return new UserDto(
                user.getId(),
                user.getFirstname(),
                user.getLastname(),
                user.getEmail(),
                user.getUsername(),
                user.getAvatarColor()
        );
    }
}
