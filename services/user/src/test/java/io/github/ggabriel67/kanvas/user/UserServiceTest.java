package io.github.ggabriel67.kanvas.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Unit Tests")
class UserServiceTest
{
    @Mock private UserMapper userMapper;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void shouldReturnCurrentAuthenticatedUserDto() {
        SecurityContext context = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);
        when(context.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(context);

        User user = User.builder().id(1).firstname("John").lastname("Doe")
                .email("john@example.com").username("johndoe").avatarColor("#AABBCC")
                .build();

        when(authentication.getPrincipal()).thenReturn(user);

        UserDto expectedDto = new UserDto(user.getId(), user.getFirstname(), user.getLastname(),
                user.getEmail(), user.getDisplayUsername(), user.getAvatarColor()
        );

        when(userMapper.toUserDto(any(User.class))).thenReturn(expectedDto);

        UserDto result = userService.getUser();

        assertThat(result).isEqualTo(expectedDto);
        verify(userMapper).toUserDto(any(User.class));
    }

    @Test
    void shouldSearchUsersAndReturnMappedDtos() {
        String query = "john";
        User user1 = User.builder().id(1).firstname("John").lastname("Doe").build();
        User user2 = User.builder().id(2).firstname("Johnny").lastname("Smith").build();

        List<User> foundUsers = List.of(user1, user2);

        when(userRepository.searchUsers(eq(query), any(PageRequest.class))).thenReturn(foundUsers);

        UserDto dto1 = new UserDto(1, "John", "Doe", "john@example.com", "johndoe", "#ABC");
        UserDto dto2 = new UserDto(2, "Johnny", "Smith", "johnny@example.com", "johnny", "#DEF");
        UserDto dto3 = new UserDto(2, "Jack", "Jones", "jackjones@example.com", "jack", "#DEF");

        when(userMapper.toUserDto(user1)).thenReturn(dto1);
        when(userMapper.toUserDto(user2)).thenReturn(dto2);
        when(userMapper.toUserDto(user2)).thenReturn(dto3);

        List<UserDto> result = userService.searchUsers(query);

        assertThat(result.size() == 2 && result.containsAll(List.of(dto1, dto2)));

        verify(userRepository).searchUsers(eq(query), any(PageRequest.class));
        verify(userMapper).toUserDto(user1);
        verify(userMapper).toUserDto(user2);
    }
}