package com.bell_ringer.services.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * DTOs for User operations
 */
public class UserRequest {

  /**
   * Request DTO for creating a new user
   */
  public record Create(
      @NotBlank(message = "Email is required") @Email(message = "Email must be valid") String email) {
  }

  /**
   * Request DTO for updating an existing user
   */
  public record Update(
      @Email(message = "Email must be valid") String email,
      Boolean emailVerified,
      String displayName,
      String photoUrl) {
  }

  /**
   * Request DTO for updating user profile (Firebase authenticated users)
   */
  public record UpdateProfile(
      String displayName,
      String photoUrl) {
  }
}
