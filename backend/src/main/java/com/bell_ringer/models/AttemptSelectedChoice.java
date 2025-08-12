package com.bell_ringer.models;

import java.time.OffsetDateTime;

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
}
