package com.bell_ringer.controllers;

import com.bell_ringer.models.User;
import com.bell_ringer.services.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MeController {
  private final UserService userService;

  public MeController(UserService userService) {
    this.userService = userService;
  }

  @GetMapping("/me")
  public User me() {
    return userService.getCurrentUserOrThrow();
  }
}