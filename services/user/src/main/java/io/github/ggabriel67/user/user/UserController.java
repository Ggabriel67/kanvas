package io.github.ggabriel67.user.user;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController
{
    private final UserService userService;

    @GetMapping("/{user-email}")
    public ResponseEntity<UserDto> getUser(@PathVariable("user-email") String userEmail) {
        return ResponseEntity.ok(userService.getUser(userEmail));
    }
}
