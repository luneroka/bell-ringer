package com.bell_ringer.services.dto;

import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO for Quiz entity to transfer quiz data without exposing internal entity
 * structure.
 * Used for API responses and requests to avoid direct entity exposure.
 */
public record QuizDto(
    Long id,

    @NotNull UUID userId,

    @NotNull Long categoryId,

    String categoryName,

    List<Long> questionIds,

    OffsetDateTime createdAt,

    OffsetDateTime completedAt,

    boolean isCompleted) {

  /**
   * Creates a QuizDto for requests when creating new quizzes (without ID and
   * timestamps).
   */
  public static QuizDto forCreation(UUID userId, Long categoryId) {
    return new QuizDto(null, userId, categoryId, null, null, null, null, false);
  }

  /**
   * Creates a QuizDto for responses (with ID and timestamps) without question
   * IDs.
   */
  public static QuizDto forResponse(Long id, UUID userId, Long categoryId, String categoryName,
      OffsetDateTime createdAt, OffsetDateTime completedAt) {
    boolean isCompleted = completedAt != null;
    return new QuizDto(id, userId, categoryId, categoryName, null, createdAt, completedAt, isCompleted);
  }

  /**
   * Creates a QuizDto for responses with question IDs included.
   */
  public static QuizDto forResponseWithQuestions(Long id, UUID userId, Long categoryId, String categoryName,
      List<Long> questionIds, OffsetDateTime createdAt,
      OffsetDateTime completedAt) {
    boolean isCompleted = completedAt != null;
    return new QuizDto(id, userId, categoryId, categoryName, questionIds, createdAt, completedAt, isCompleted);
  }
}
