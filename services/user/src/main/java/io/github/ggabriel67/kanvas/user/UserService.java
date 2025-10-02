package io.github.ggabriel67.kanvas.user;

import io.github.ggabriel67.kanvas.security.JwtService;
import io.github.ggabriel67.kanvas.token.TokenType;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService
{
    private final UserMapper userMapper;
    private final UserRepository userRepository;

    public UserDto getUser() {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userMapper.toUserDto(user);
    }

    public List<UserDto> searchUsers(String query) {
        return userRepository.searchUsers(query, PageRequest.of(0, 10))
                .stream()
                .map(userMapper::toUserDto)
                .toList();
    }
}
