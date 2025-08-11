package com.bell_ringer.models.id;

import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class QuizQuestionId implements Serializable {
  private Long quizId;
  private Long questionId;

  public QuizQuestionId() {}

  public QuizQuestionId(Long quizId, Long questionId) {
    this.quizId = quizId;
    this.questionId = questionId;
  }

  public Long getQuizId() { return quizId; }
  public Long getQuestionId() { return questionId; }

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof QuizQuestionId that)) return false;
    return Objects.equals(quizId, that.quizId) && Objects.equals(questionId, that.questionId);
  }
  @Override public int hashCode() { return Objects.hash(quizId, questionId); }
}
