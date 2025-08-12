package com.bell_ringer.services.dto;

import java.util.UUID;

/**
 * Response DTO for User operations
 */
public record UserResponse(
    UUID id,
    String email) {
}
