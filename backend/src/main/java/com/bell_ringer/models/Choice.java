package com.bell_ringer.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(
  name = "choices",
  indexes = {
    @Index(name = "idx_choices_question", columnList = "question_id")
  },
  uniqueConstraints = {
    @jakarta.persistence.UniqueConstraint(name = "uq_choice", columnNames = {"question_id", "choice_text"})
  }
)
public class Choice {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "question_id", nullable = false)
  private Question question;

  @NotBlank
  @Column(name = "choice_text", nullable = false, columnDefinition = "TEXT")
  private String choiceText;

  @Column(name = "is_correct", nullable = false)
    private boolean isCorrect = false;

    public Choice() {}

    public Long getId() {
      return id;
    }

    public void setId(Long id) {
      this.id = id;
    }

    public Question getQuestion() {
      return question;
    }

    public void setQuestion(Question question) {
      this.question = question;
    }

    public String getChoiceText() {
      return choiceText;
    }

    public void setChoiceText(String choiceText) {
      this.choiceText = choiceText;
    }

    public boolean isCorrect() {
      return isCorrect;
    }

    public void setCorrect(boolean correct) {
      isCorrect = correct;
    }
}
