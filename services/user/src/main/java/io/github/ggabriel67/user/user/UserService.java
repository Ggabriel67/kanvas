package io.github.ggabriel67.user.user;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService
{
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserDto getUser(String userEmail) {
        return userRepository.findByEmail(userEmail)
                .map(userMapper::toUserDto)
                .orElseThrow(() -> new UsernameNotFoundException("User with email " + userEmail + " not found"));
    }
}
