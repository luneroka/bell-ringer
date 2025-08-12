package com.bell_ringer.services.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * DTOs for Attempt operations
 */
public class AttemptRequest {

  /**
   * Request DTO for retrying a quiz (creating a new attempt for an existing
   * quiz).
   * Note: Initial attempts are created automatically via question generation.
   */
  public record Create(
      @NotNull(message = "Quiz ID is required") Long quizId) {
  }

  /**
   * Request DTO for submitting selected choices for an attempt
   */
  public record SubmitChoices(
      @NotNull(message = "Selected choices are required") @NotEmpty(message = "At least one choice must be selected") @Valid List<SelectedChoiceSubmission> selectedChoices) {

    public record SelectedChoiceSubmission(
        @NotNull(message = "Question ID is required") Long questionId,
        @NotNull(message = "Choice ID is required") Long choiceId) {
    }
  }

  /**
   * Request DTO for submitting text answers for an attempt
   */
  public record SubmitTextAnswers(
      @NotNull(message = "Text answers are required") @NotEmpty(message = "At least one text answer must be provided") @Valid List<TextAnswerSubmission> textAnswers) {

    public record TextAnswerSubmission(
        @NotNull(message = "Question ID is required") Long questionId,
        @NotNull(message = "Answer text is required") @Size(min = 1, max = 5000, message = "Answer text must be between 1 and 5000 characters") String answerText) {
    }
  }

  /**
   * Request DTO for scoring a text answer (admin/teacher use)
   */
  public record ScoreTextAnswer(
      @NotNull(message = "Question ID is required") Long questionId,
      Integer score,
      Boolean isCorrect,
      @Size(max = 2000, message = "Feedback must not exceed 2000 characters") String feedback) {
  }
}
