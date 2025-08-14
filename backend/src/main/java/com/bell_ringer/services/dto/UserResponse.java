package com.bell_ringer.services.dto;

import com.bell_ringer.models.User;
import java.util.UUID;

/**
 * Response DTO for User operations
 */
public record UserResponse(
        UUID id,
        String authProvider,
        String authUid,
        String email,
        Boolean emailVerified,
        String displayName,
        String photoUrl) {

    /**
     * Create UserResponse from User entity
     */
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getAuthProvider(),
                user.getAuthUid(),
                user.getEmail(),
                user.getEmailVerified(),
                user.getDisplayName(),
                user.getPhotoUrl());
    }
}
