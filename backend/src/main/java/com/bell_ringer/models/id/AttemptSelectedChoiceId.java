package com.bell_ringer.models.id;

import java.io.Serializable;
import java.util.Objects;

/**
 * Composite primary key for AttemptSelectedChoice entity.
 * Represents the combination of attempt_id, question_id, and choice_id.
 */
public class AttemptSelectedChoiceId implements Serializable {

  private Long attemptId;
  private Long questionId;
  private Long choiceId;

  public AttemptSelectedChoiceId() {
  }

  public AttemptSelectedChoiceId(Long attemptId, Long questionId, Long choiceId) {
    this.attemptId = attemptId;
    this.questionId = questionId;
    this.choiceId = choiceId;
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

  public Long getChoiceId() {
    return choiceId;
  }

  public void setChoiceId(Long choiceId) {
    this.choiceId = choiceId;
  }

  // equals() and hashCode() are REQUIRED for composite keys
  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    AttemptSelectedChoiceId that = (AttemptSelectedChoiceId) o;
    return Objects.equals(attemptId, that.attemptId) &&
        Objects.equals(questionId, that.questionId) &&
        Objects.equals(choiceId, that.choiceId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(attemptId, questionId, choiceId);
  }

  @Override
  public String toString() {
    return "AttemptSelectedChoiceId{" +
        "attemptId=" + attemptId +
        ", questionId=" + questionId +
        ", choiceId=" + choiceId +
        '}';
  }
}
