package com.bell_ringer.services.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

/**
 * DTOs for Quiz operations
 */
public class QuizRequest {

  /**
   * Request DTO for creating a new quiz
   */
  public record Create(
      @NotNull(message = "User ID is required") UUID userId,
      @NotNull(message = "Category ID is required") Long categoryId) {
  }

  /**
   * Request DTO for creating a quiz with predefined questions
   */
  public record CreateWithQuestions(
      @NotNull(message = "User ID is required") UUID userId,
      @NotNull(message = "Category ID is required") Long categoryId,
      @NotNull(message = "Question IDs are required") List<Long> questionIds) {
  }
}
