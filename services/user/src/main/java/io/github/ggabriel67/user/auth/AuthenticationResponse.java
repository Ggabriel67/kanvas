package io.github.ggabriel67.user.auth;

import lombok.Builder;

@Builder
public record AuthenticationResponse(
        String token
) {

}
