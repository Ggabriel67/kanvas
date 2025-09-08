package io.github.ggabriel67.kanvas.user;

import io.github.ggabriel67.kanvas.exception.UserNotFoundException;
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

    public User getUserById(Integer id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }
}
