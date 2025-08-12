package com.bell_ringer.services.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * DTOs for QuizQuestion operations
 */
public class QuizQuestionRequest {

  /**
   * Request DTO for adding multiple questions to a quiz
   */
  public record AddMany(
      @NotNull(message = "Question IDs are required") @NotEmpty(message = "Question IDs must not be empty") List<Long> questionIds) {
  }

  /**
   * Request DTO for replacing all questions in a quiz with a new ordered list
   */
  public record ReplaceAll(
      List<Long> questionIds // Can be null or empty to clear all
  ) {
  }
}
