package com.bell_ringer.controllers;

import com.bell_ringer.services.UserService;
import com.bell_ringer.services.dto.UserRequest;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.UUID;
import com.bell_ringer.models.User;
import java.util.stream.StreamSupport;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {
  private final UserService userService;

  // Constructor
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

  // UPDATE USER
  @PutMapping("/{id}")
  public String updateUser(@PathVariable UUID id, @RequestBody UserRequest.Update req) {
    if (req == null) {
      throw new IllegalArgumentException("request body is required");
    }

    User updatedUser = userService.updateUser(id, req.email(), req.emailVerified(), req.displayName(), req.photoUrl());
    return updatedUser.getEmail();
  }

  // DELETE USER
  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteUser(@PathVariable UUID id) {
    userService.deleteUser(id);
  }

  // PING TO TEST CONNECTION
  @GetMapping("/ping")
  public String ping() {
    return "ok";
  }
}
