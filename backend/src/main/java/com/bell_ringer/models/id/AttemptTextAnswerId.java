package com.bell_ringer.models.id;

import java.io.Serializable;
import java.util.Objects;

/**
 * Composite primary key for AttemptTextAnswer entity.
 * Represents the combination of attempt_id and question_id.
 */
public class AttemptTextAnswerId implements Serializable {

  private Long attemptId;
  private Long questionId;

  public AttemptTextAnswerId() {
  }

  public AttemptTextAnswerId(Long attemptId, Long questionId) {
    this.attemptId = attemptId;
    this.questionId = questionId;
  }

  // Getters and setters
  public Long getAttemptId() {
    return attemptId;
  }

  public void setAttemptId(Long attemptId) {
    this.attemptId = attemptId;
  }

  public Long getQuestionId() {
    return questionId;
  }

  public void setQuestionId(Long questionId) {
    this.questionId = questionId;
  }

  // equals() and hashCode() are REQUIRED for composite keys
  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    AttemptTextAnswerId that = (AttemptTextAnswerId) o;
    return Objects.equals(attemptId, that.attemptId) &&
        Objects.equals(questionId, that.questionId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(attemptId, questionId);
  }

  @Override
  public String toString() {
    return "AttemptTextAnswerId{" +
        "attemptId=" + attemptId +
        ", questionId=" + questionId +
        '}';
  }
}
