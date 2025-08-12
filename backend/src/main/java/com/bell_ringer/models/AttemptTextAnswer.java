package com.bell_ringer.models;

import java.time.OffsetDateTime;
import java.util.Objects;

import org.hibernate.annotations.CreationTimestamp;

import com.bell_ringer.models.id.AttemptTextAnswerId;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "attempt_text_answers")
@IdClass(AttemptTextAnswerId.class)
public class AttemptTextAnswer {

  @Id
  @Column(name = "attempt_id")
  private Long attemptId;

  @Id
  @Column(name = "question_id")
  private Long questionId;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "attempt_id", insertable = false, updatable = false)
  private Attempt attempt;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "quiz_id", nullable = false)
  private Quiz quiz;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "question_id", insertable = false, updatable = false)
  private Question question;

  @Column(name = "answer_text", nullable = false, columnDefinition = "text")
  private String answerText;

  @Column(name = "score")
  private Integer score;

  @Column(name = "is_correct")
  private Boolean isCorrect;

  @Column(name = "feedback", columnDefinition = "text")
  private String feedback;

  @CreationTimestamp
  @Column(name = "answered_at", nullable = false, updatable = false)
  private OffsetDateTime answeredAt;

  // Constructors
  public AttemptTextAnswer() {
  }

  public AttemptTextAnswer(Long attemptId, Long questionId, String answerText) {
    this.attemptId = attemptId;
    this.questionId = questionId;
    this.answerText = answerText;
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

  public Attempt getAttempt() {
    return attempt;
  }

  public void setAttempt(Attempt attempt) {
    this.attempt = attempt;
  }

  public Quiz getQuiz() {
    return quiz;
  }

  public void setQuiz(Quiz quiz) {
    this.quiz = quiz;
  }

  public Question getQuestion() {
    return question;
  }

  public void setQuestion(Question question) {
    this.question = question;
  }

  public String getAnswerText() {
    return answerText;
  }

  public void setAnswerText(String answerText) {
    this.answerText = answerText;
  }

  public Integer getScore() {
    return score;
  }

  public void setScore(Integer score) {
    this.score = score;
  }

  public Boolean getIsCorrect() {
    return isCorrect;
  }

  public void setIsCorrect(Boolean isCorrect) {
    this.isCorrect = isCorrect;
  }

  public String getFeedback() {
    return feedback;
  }

  public void setFeedback(String feedback) {
    this.feedback = feedback;
  }

  public OffsetDateTime getAnsweredAt() {
    return answeredAt;
  }

  public void setAnsweredAt(OffsetDateTime answeredAt) {
    this.answeredAt = answeredAt;
  }

  // Helper methods
  public boolean isScored() {
    return score != null;
  }

  public boolean hasResult() {
    return isCorrect != null;
  }

  public boolean hasFeedback() {
    return feedback != null && !feedback.trim().isEmpty();
  }

  // Equals and hashCode based on composite key
  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    AttemptTextAnswer that = (AttemptTextAnswer) o;
    return Objects.equals(attemptId, that.attemptId) &&
        Objects.equals(questionId, that.questionId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(attemptId, questionId);
  }

  @Override
  public String toString() {
    return "AttemptTextAnswer{" +
        "attemptId=" + attemptId +
        ", questionId=" + questionId +
        ", answerText='" + answerText + '\'' +
        ", score=" + score +
        ", isCorrect=" + isCorrect +
        ", answeredAt=" + answeredAt +
        '}';
  }
}
