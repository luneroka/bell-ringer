package com.bell_ringer.services.dto;

import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;

/**
 * DTO for AttemptSelectedChoice entity to transfer selected choice data without
 * exposing internal entity structure.
 * Used for tracking user choice selections in quiz attempts.
 */
public record AttemptSelectedChoiceDto(
    @NotNull Long attemptId,

    @NotNull Long questionId,

    @NotNull Long choiceId,

    @NotNull Long quizId,

    OffsetDateTime selectedAt) {

  /**
   * Creates an AttemptSelectedChoiceDto for responses.
   */
  public static AttemptSelectedChoiceDto forResponse(Long attemptId, Long questionId, Long choiceId,
      Long quizId, OffsetDateTime selectedAt) {
    return new AttemptSelectedChoiceDto(attemptId, questionId, choiceId, quizId, selectedAt);
  }

  /**
   * Creates an AttemptSelectedChoiceDto for creation (without timestamp).
   */
  public static AttemptSelectedChoiceDto forCreation(Long attemptId, Long questionId, Long choiceId, Long quizId) {
    return new AttemptSelectedChoiceDto(attemptId, questionId, choiceId, quizId, null);
  }
}
