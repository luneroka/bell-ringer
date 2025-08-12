package com.bell_ringer.services.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * DTOs for AttemptSelectedChoice operations
 */
public class AttemptSelectedChoiceRequest {

  /**
   * Request DTO for submitting a single choice selection
   */
  public record Submit(
      @NotNull(message = "Attempt ID is required") Long attemptId,
      @NotNull(message = "Question ID is required") Long questionId,
      @NotNull(message = "Choice ID is required") Long choiceId) {
  }

  /**
   * Request DTO for submitting multiple choice selections in batch
   */
  public record SubmitBatch(
      @NotNull(message = "Attempt ID is required") Long attemptId,
      @NotNull(message = "Selected choices are required") @NotEmpty(message = "At least one choice must be selected") @Valid List<ChoiceSelection> selectedChoices) {

    public record ChoiceSelection(
        @NotNull(message = "Question ID is required") Long questionId,
        @NotNull(message = "Choice ID is required") Long choiceId) {
    }
  }

  /**
   * Request DTO for updating a choice selection (changing answer)
   */
  public record Update(
      @NotNull(message = "New choice ID is required") Long newChoiceId) {
  }
}
