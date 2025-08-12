package com.bell_ringer.services.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * DTOs for AttemptTextAnswer operations
 */
public class AttemptTextAnswerRequest {

  /**
   * Request DTO for submitting a single text answer
   */
  public record Submit(
      @NotNull(message = "Attempt ID is required") Long attemptId,
      @NotNull(message = "Question ID is required") Long questionId,
      @NotNull(message = "Answer text is required") @Size(min = 1, max = 5000, message = "Answer text must be between 1 and 5000 characters") String answerText) {
  }

  /**
   * Request DTO for submitting multiple text answers in batch
   */
  public record SubmitBatch(
      @NotNull(message = "Attempt ID is required") Long attemptId,
      @NotNull(message = "Text answers are required") @NotEmpty(message = "At least one text answer must be provided") @Valid List<TextAnswerSubmission> textAnswers) {

    public record TextAnswerSubmission(
        @NotNull(message = "Question ID is required") Long questionId,
        @NotNull(message = "Answer text is required") @Size(min = 1, max = 5000, message = "Answer text must be between 1 and 5000 characters") String answerText) {
    }
  }

  /**
   * Request DTO for updating a text answer (before grading)
   */
  public record Update(
      @NotNull(message = "Answer text is required") @Size(min = 1, max = 5000, message = "Answer text must be between 1 and 5000 characters") String answerText) {
  }

  /**
   * Request DTO for scoring/grading a text answer (admin/teacher use)
   */
  public record Grade(
      Integer score,
      Boolean isCorrect,
      @Size(max = 2000, message = "Feedback must not exceed 2000 characters") String feedback) {
  }
}
