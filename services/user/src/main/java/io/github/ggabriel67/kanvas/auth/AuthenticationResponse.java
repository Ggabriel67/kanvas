package io.github.ggabriel67.kanvas.auth;

import lombok.Builder;

@Builder
public record AuthenticationResponse(
        String accessToken
) {

}
