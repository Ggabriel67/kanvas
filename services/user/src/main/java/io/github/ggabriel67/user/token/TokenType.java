package io.github.ggabriel67.user.token;

import lombok.Getter;

@Getter
public enum TokenType {
    ACCESS("accessToken"),
    REFRESH("refreshToken")
    ;
    private final String name;

    TokenType(String name) {
        this.name = name;
    }
}
