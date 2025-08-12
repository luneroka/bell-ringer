package com.bell_ringer.models;

import java.time.OffsetDateTime;
import java.util.Objects;

import org.hibernate.annotations.CreationTimestamp;

import com.bell_ringer.models.id.AttemptSelectedChoiceId;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "attempt_selected_choices")
@IdClass(AttemptSelectedChoiceId.class)
public class AttemptSelectedChoice {

  @Id
  @Column(name = "attempt_id")
  private Long attemptId;

  @Id
  @Column(name = "question_id")
  private Long questionId;

  @Id
  @Column(name = "choice_id")
  private Long choiceId;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "attempt_id", insertable = false, updatable = false)
  private Attempt attempt;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "quiz_id", nullable = false)
  private Quiz quiz;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "question_id", insertable = false, updatable = false)
  private Question question;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "choice_id", insertable = false, updatable = false)
  private Choice choice;

  @CreationTimestamp
  @Column(name = "selected_at", nullable = false, updatable = false)
  private OffsetDateTime selectedAt;

  // Constructors
  public AttemptSelectedChoice() {
  }

  public AttemptSelectedChoice(Long attemptId, Long questionId, Long choiceId) {
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

  public Choice getChoice() {
    return choice;
  }

  public void setChoice(Choice choice) {
    this.choice = choice;
  }

  public OffsetDateTime getSelectedAt() {
    return selectedAt;
  }

  public void setSelectedAt(OffsetDateTime selectedAt) {
    this.selectedAt = selectedAt;
  }

  // Equals and hashCode based on composite key
  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    AttemptSelectedChoice that = (AttemptSelectedChoice) o;
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
    return "AttemptSelectedChoice{" +
        "attemptId=" + attemptId +
        ", questionId=" + questionId +
        ", choiceId=" + choiceId +
        ", selectedAt=" + selectedAt +
        '}';
  }
}
