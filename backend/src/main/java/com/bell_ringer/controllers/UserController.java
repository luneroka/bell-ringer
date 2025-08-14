package com.bell_ringer.controllers;

import com.bell_ringer.services.UserService;
import com.bell_ringer.services.dto.UserRequest;
import com.bell_ringer.services.dto.UserResponse;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import java.util.UUID;
import com.bell_ringer.models.User;
import java.util.stream.StreamSupport;

@RestController
public class UserController {
  private final UserService userService;

  // Constructor
  public UserController(UserService userService) {
    this.userService = userService;
  }

  // ============================================
  // CONVENIENCE ENDPOINT (Non-versioned)
  // ============================================

  /**
   * Convenience endpoint for current user - short URL /me
   */
  @GetMapping("/me")
  public ResponseEntity<UserResponse> getCurrentUserConvenience() {
    try {
      User user = userService.getCurrentUserOrThrow();
      return ResponseEntity.ok(UserResponse.from(user));
    } catch (IllegalStateException e) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  // ============================================
  // VERSIONED API ENDPOINTS
  // ============================================

  /**
   * Get current authenticated user - automatically syncs with Firebase token
   */
  @GetMapping("/api/v1/users/me")
  public ResponseEntity<UserResponse> getCurrentUser() {
    try {
      User user = userService.getCurrentUserOrThrow();
      return ResponseEntity.ok(UserResponse.from(user));
    } catch (IllegalStateException e) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  /**
   * Update current user's profile (Firebase authenticated users only)
   */
  @PutMapping("/api/v1/users/me")
  public ResponseEntity<UserResponse> updateCurrentUserProfile(@RequestBody UserRequest.UpdateProfile request) {
    try {
      User currentUser = userService.getCurrentUserOrThrow();

      // Update the user with new profile information
      User updatedUser = userService.updateUser(
          currentUser.getId(),
          null, // Don't update email through this endpoint
          null, // Don't update email verification through this endpoint
          request.displayName(),
          request.photoUrl());

      return ResponseEntity.ok(UserResponse.from(updatedUser));
    } catch (IllegalStateException e) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  // ============================================
  // ADMIN USER OPERATIONS (ID-based)
  // ============================================

  // GET ALL USERS - Return full user info for authenticated endpoints
  @GetMapping("/api/v1/users")
  public Iterable<UserResponse> getAllUsers() {
    return StreamSupport.stream(userService.getAllUsers().spliterator(), false)
        .map(UserResponse::from)
        .toList();
  }

  // GET USER BY EMAIL
  @GetMapping("/api/v1/users/email/{email}")
  public UserResponse getUserByEmail(@PathVariable String email) {
    return userService.getUserByEmail(email)
        .map(UserResponse::from)
        .orElse(null);
  }

  // GET USER BY ID
  @GetMapping("/api/v1/users/{id}")
  public UserResponse getUserById(@PathVariable UUID id) {
    return UserResponse.from(userService.getUserById(id));
  }

  // UPDATE USER (Admin operation)
  @PutMapping("/api/v1/users/{id}")
  public UserResponse updateUser(@PathVariable UUID id, @RequestBody UserRequest.Update req) {
    if (req == null) {
      throw new IllegalArgumentException("request body is required");
    }

    User updatedUser = userService.updateUser(id, req.email(), req.emailVerified(), req.displayName(), req.photoUrl());
    return UserResponse.from(updatedUser);
  }

  // DELETE USER
  @DeleteMapping("/api/v1/users/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteUser(@PathVariable UUID id) {
    userService.deleteUser(id);
  }

  // PING TO TEST CONNECTION
  @GetMapping("/api/v1/users/ping")
  public String ping() {
    return "ok";
  }

  // ROOT ENDPOINT FOR BASIC CONNECTIVITY TEST
  @GetMapping("/")
  public String root() {
    return "Bell-Ringer API is running! Use /actuator/health for health checks.";
  }
}
