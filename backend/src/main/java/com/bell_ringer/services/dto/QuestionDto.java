package com.bell_ringer.services.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * DTO for Question entity to transfer question data without exposing internal
 * entity structure.
 * Used for API responses and requests to avoid direct entity exposure.
 */
public record QuestionDto(
    Long id,

    @NotNull String type,

    @NotNull Long categoryId,

    @NotNull String difficulty,

    @NotBlank String question,

    List<ChoiceDto> choices,

    OffsetDateTime createdAt,

    OffsetDateTime updatedAt) {

  /**
   * Creates a QuestionDto for requests when creating new questions (without ID
   * and timestamps).
   */
  public static QuestionDto forCreation(String type, Long categoryId, String difficulty, String question,
      List<ChoiceDto> choices) {
    return new QuestionDto(null, type, categoryId, difficulty, question, choices, null, null);
  }

  /**
   * Creates a QuestionDto for responses (with ID and timestamps).
   */
  public static QuestionDto forResponse(Long id, String type, Long categoryId, String difficulty,
      String question, List<ChoiceDto> choices,
      OffsetDateTime createdAt, OffsetDateTime updatedAt) {
    return new QuestionDto(id, type, categoryId, difficulty, question, choices, createdAt, updatedAt);
  }

  /**
   * Creates a QuestionDto for responses without choices (for performance when
   * choices aren't needed).
   */
  public static QuestionDto forResponseWithoutChoices(Long id, String type, Long categoryId, String difficulty,
      String question, OffsetDateTime createdAt, OffsetDateTime updatedAt) {
    return new QuestionDto(id, type, categoryId, difficulty, question, null, createdAt, updatedAt);
  }
}
