package com.bell_ringer.models;

import java.time.OffsetDateTime;

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
}
