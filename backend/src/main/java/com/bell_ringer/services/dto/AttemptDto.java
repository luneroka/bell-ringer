package com.bell_ringer.services.dto;

import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * DTO for Attempt entity to transfer attempt data without exposing internal
 * entity structure.
 * Used for API responses to avoid direct entity exposure.
 */
public record AttemptDto(
    Long id,

    @NotNull Long quizId,

    OffsetDateTime startedAt,

    OffsetDateTime completedAt,

    boolean isCompleted,

    List<AttemptSelectedChoiceDto> selectedChoices,

    List<AttemptTextAnswerDto> textAnswers) {

  /**
   * Creates an AttemptDto for responses without answer details (basic view).
   */
  public static AttemptDto forResponse(Long id, Long quizId, OffsetDateTime startedAt, OffsetDateTime completedAt) {
    boolean isCompleted = completedAt != null;
    return new AttemptDto(id, quizId, startedAt, completedAt, isCompleted, null, null);
  }

  /**
   * Creates an AttemptDto for responses with answer details included (detailed
   * view).
   */
  public static AttemptDto forResponseWithAnswers(Long id, Long quizId, OffsetDateTime startedAt,
      OffsetDateTime completedAt, List<AttemptSelectedChoiceDto> selectedChoices,
      List<AttemptTextAnswerDto> textAnswers) {
    boolean isCompleted = completedAt != null;
    return new AttemptDto(id, quizId, startedAt, completedAt, isCompleted, selectedChoices, textAnswers);
  }

  /**
   * Nested DTO for selected choices within an attempt
   */
  public record AttemptSelectedChoiceDto(
      Long attemptId,
      Long questionId,
      Long choiceId,
      OffsetDateTime selectedAt) {

    public static AttemptSelectedChoiceDto from(Long attemptId, Long questionId, Long choiceId,
        OffsetDateTime selectedAt) {
      return new AttemptSelectedChoiceDto(attemptId, questionId, choiceId, selectedAt);
    }
  }

  /**
   * Nested DTO for text answers within an attempt
   */
  public record AttemptTextAnswerDto(
      Long attemptId,
      Long questionId,
      String answerText,
      Integer score,
      Boolean isCorrect,
      String feedback,
      OffsetDateTime answeredAt) {

    public static AttemptTextAnswerDto from(Long attemptId, Long questionId, String answerText,
        Integer score, Boolean isCorrect, String feedback, OffsetDateTime answeredAt) {
      return new AttemptTextAnswerDto(attemptId, questionId, answerText, score, isCorrect, feedback, answeredAt);
    }

    public boolean isScored() {
      return score != null;
    }

    public boolean hasResult() {
      return isCorrect != null;
    }

    public boolean hasFeedback() {
      return feedback != null && !feedback.trim().isEmpty();
    }
  }
}
