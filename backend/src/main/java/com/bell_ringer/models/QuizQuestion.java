package com.bell_ringer.models;

import com.bell_ringer.models.id.QuizQuestionId;
import jakarta.persistence.*;

@Entity
@Table(name = "quiz_questions")
public class QuizQuestion {

  @EmbeddedId
  private QuizQuestionId id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @MapsId("quizId") // maps PK part to this FK
  @JoinColumn(name = "quiz_id", nullable = false)
  private Quiz quiz;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @MapsId("questionId")
  @JoinColumn(name = "question_id", nullable = false)
  private Question question;

  public QuizQuestion() {}

  public QuizQuestion(Quiz quiz, Question question) {
    this.quiz = quiz;
    this.question = question;
    this.id = new QuizQuestionId(quiz.getId(), question.getId());
  }

  public QuizQuestionId getId() { return id; }
  public Quiz getQuiz() { return quiz; }
  public Question getQuestion() { return question; }

  public void setQuiz(Quiz quiz) {
    this.quiz = quiz;
    if (this.id == null && quiz != null && this.question != null) {
      this.id = new QuizQuestionId(quiz.getId(), this.question.getId());
    } else if (this.id != null && quiz != null) {
      this.id = new QuizQuestionId(quiz.getId(), this.id.getQuestionId());
    }
  }

  public void setQuestion(Question question) {
    this.question = question;
    if (this.id == null && this.quiz != null && question != null) {
      this.id = new QuizQuestionId(this.quiz.getId(), question.getId());
    } else if (this.id != null && question != null) {
      this.id = new QuizQuestionId(this.id.getQuizId(), question.getId());
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    QuizQuestion that = (QuizQuestion) o;
    return java.util.Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return java.util.Objects.hash(id);
  }

  @Override
  public String toString() {
    return "QuizQuestion{" +
        "quizId=" + (id != null ? id.getQuizId() : null) +
        ", questionId=" + (id != null ? id.getQuestionId() : null) +
        '}';
  }
}
