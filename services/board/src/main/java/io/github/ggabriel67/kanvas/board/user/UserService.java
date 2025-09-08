package io.github.ggabriel67.kanvas.board.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService
{
    private final UserRepository userRepository;

    public void saveUser(UserDto userDto) {
        userRepository.save(
                User.builder()
                        .id(userDto.id())
                        .firstname(userDto.firstname())
                        .lastname(userDto.lastname())
                        .email(userDto.email())
                        .username(userDto.username())
                        .avatarColor(userDto.avatarColor())
                        .build());
    }
}
