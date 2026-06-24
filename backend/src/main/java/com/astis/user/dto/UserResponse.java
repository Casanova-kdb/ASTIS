package com.astis.user.dto;

import com.astis.user.entity.AppUser;

public record UserResponse(
        Long id,
        String username,
        String email,
        String displayName,
        String preferredStudyTime
) {
    public static UserResponse from(AppUser user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getDisplayName(),
                user.getPreferredStudyTime()
        );
    }
}
