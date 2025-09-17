package io.github.ggabriel67.kanvas.user;

import io.github.ggabriel67.kanvas.event.user.UserCreated;
import io.github.ggabriel67.kanvas.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService
{
    private final UserRepository userRepository;

    public void saveUser(UserCreated user) {
        userRepository.save(
                User.builder()
                        .id(user.id())
                        .firstname(user.firstname())
                        .lastname(user.lastname())
                        .email(user.email())
                        .username(user.username())
                        .avatarColor(user.avatarColor())
                        .build());
    }

    public User getUserById(Integer id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }
}
