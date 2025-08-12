package com.bell_ringer.services.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO for Choice entity to transfer choice data without exposing internal
 * entity structure.
 * Used for API responses and requests to avoid direct entity exposure.
 */
public record ChoiceDto(
    Long id,

    @NotNull Long questionId,

    @NotBlank String choiceText,

    boolean isCorrect) {

  /**
   * Creates a ChoiceDto for requests when creating new choices (without ID).
   */
  public static ChoiceDto forCreation(Long questionId, String choiceText, boolean isCorrect) {
    return new ChoiceDto(null, questionId, choiceText, isCorrect);
  }

  /**
   * Creates a ChoiceDto for responses (with ID).
   */
  public static ChoiceDto forResponse(Long id, Long questionId, String choiceText, boolean isCorrect) {
    return new ChoiceDto(id, questionId, choiceText, isCorrect);
  }
}
