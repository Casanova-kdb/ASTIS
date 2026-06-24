package com.astis.user.dto;

public record AuthResponse(
        String tokenType,
        String accessToken,
        UserResponse user
) {
    public static AuthResponse bearer(String accessToken, UserResponse user) {
        return new AuthResponse("Bearer", accessToken, user);
    }
}
