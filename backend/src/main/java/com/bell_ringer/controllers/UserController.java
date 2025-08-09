package com.bell_ringer.controllers;

import com.bell_ringer.services.UserService;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;
import com.bell_ringer.models.User;
import java.util.stream.StreamSupport;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {
  private final UserService userService;

  //  Constructor
  public UserController(UserService userService) {
    this.userService = userService;
  }

  // GET ALL USERS
  @GetMapping
  public Iterable<String> getAllUsers() {
    return StreamSupport.stream(userService.getAllUsers().spliterator(), false)
        .map(User::getEmail)
        .toList();
  }

  // GET USER BY EMAIL
  @GetMapping("/email/{email}")
  public String getUserByEmail(@PathVariable String email) {
    return userService.getUserByEmail(email).map(User::getEmail).orElse(null);
  }

  // GET USER BY ID
  @GetMapping("/{id}")
  public String getUserById(@PathVariable UUID id) {
    return userService.getUserById(id).getEmail();
  }

  // CREATE USER
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public UserResponse createUser(@RequestBody CreateUserRequest req) {
    if (req == null || req.getEmail() == null || req.getEmail().isBlank()) {
        throw new IllegalArgumentException("email is required");
    }
    
    User user = userService.createUser(req.getEmail());
    return new UserResponse(user.getId(), user.getEmail());
}

  // UPDATE USER
  @PutMapping("/{id}")
  public String updateUser(@PathVariable UUID id, @RequestBody UpdateUserRequest req) {
    if (req == null) {
      throw new IllegalArgumentException("request body is required");
    }

    User updatedUser = userService.updateUser(id, req.getEmail(), req.getEmailVerified(), req.getDisplayName(), req.getPhotoUrl());
    return updatedUser.getEmail();
  }

  // DELETE USER
  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteUser(@PathVariable UUID id) {
    userService.deleteUser(id);
  }

  // *** MODELS *** //
  public static class CreateUserRequest {
    private String email;
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
  }
  
  public static class UserResponse {
    private UUID id;
    private String email;
    
    public UserResponse(UUID id, String email) {
        this.id = id;
        this.email = email;
    }
    
    // Getters
    public UUID getId() { return id; }
    public String getEmail() { return email; }
  }

  public static class UpdateUserRequest {
    private String email;
    private Boolean emailVerified;
    private String displayName;
    private String photoUrl;

    // Getters and setters
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public Boolean getEmailVerified() { return emailVerified; }
    public void setEmailVerified(Boolean emailVerified) { this.emailVerified = emailVerified; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }
  }

  // PING TO TEST CONNECTION
  @GetMapping("/ping")
  public String ping() {
    return "ok";
  }
}
