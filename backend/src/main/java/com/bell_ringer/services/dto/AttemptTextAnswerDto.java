package com.bell_ringer.services.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.OffsetDateTime;

/**
 * DTO for AttemptTextAnswer entity to transfer text answer data without
 * exposing internal entity structure.
 * Used for tracking user text responses in quiz attempts.
 */
public record AttemptTextAnswerDto(
    @NotNull Long attemptId,

    @NotNull Long questionId,

    @NotNull Long quizId,

    @NotNull @Size(min = 1, max = 5000, message = "Answer text must be between 1 and 5000 characters") String answerText,

    Integer score,

    Boolean isCorrect,

    @Size(max = 2000, message = "Feedback must not exceed 2000 characters") String feedback,

    OffsetDateTime answeredAt) {

  /**
   * Creates an AttemptTextAnswerDto for responses (includes all fields).
   */
  public static AttemptTextAnswerDto forResponse(Long attemptId, Long questionId, Long quizId,
      String answerText, Integer score, Boolean isCorrect, String feedback, OffsetDateTime answeredAt) {
    return new AttemptTextAnswerDto(attemptId, questionId, quizId, answerText, score, isCorrect, feedback, answeredAt);
  }

  /**
   * Creates an AttemptTextAnswerDto for submission (before grading).
   */
  public static AttemptTextAnswerDto forSubmission(Long attemptId, Long questionId, Long quizId, String answerText) {
    return new AttemptTextAnswerDto(attemptId, questionId, quizId, answerText, null, null, null, null);
  }

  /**
   * Creates an AttemptTextAnswerDto for grading (with score and feedback).
   */
  public static AttemptTextAnswerDto forGrading(Long attemptId, Long questionId, Long quizId,
      String answerText, Integer score, Boolean isCorrect, String feedback, OffsetDateTime answeredAt) {
    return new AttemptTextAnswerDto(attemptId, questionId, quizId, answerText, score, isCorrect, feedback, answeredAt);
  }

  // Helper methods for checking answer status
  public boolean isScored() {
    return score != null;
  }

  public boolean hasResult() {
    return isCorrect != null;
  }

  public boolean hasFeedback() {
    return feedback != null && !feedback.trim().isEmpty();
  }

  public boolean needsGrading() {
    return !isScored() && !hasResult();
  }
}
