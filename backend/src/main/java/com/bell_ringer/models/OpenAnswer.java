package com.bell_ringer.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.OnDelete;
import org.hibernate.type.SqlTypes;
import org.hibernate.annotations.Check;
import java.util.Map;

@Entity
@Table(name = "open_answers", indexes = @Index(name = "idx_open_answers_question_id", columnList = "question_id"), uniqueConstraints = @UniqueConstraint(name = "uq_open_answer", columnNames = {
    "question_id", "answer" }))
@Check(constraints = "min_score BETWEEN 0 AND 100 AND char_length(btrim(answer)) > 0")
public class OpenAnswer {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotNull
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @OnDelete(action = org.hibernate.annotations.OnDeleteAction.CASCADE)

  @JoinColumn(name = "question_id", nullable = false)
  private Question question;

  @NotBlank(message = "Answer text is required")
  @Column(name = "answer", nullable = false, columnDefinition = "TEXT")
  private String answer;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "rubric_keywords", columnDefinition = "JSONB")
  private Map<String, Object> rubricKeywords;

  @NotNull
  @Min(value = 0, message = "Minimum score must be >= 0")
  @Max(value = 100, message = "Maximum score must be <= 100")
  @Column(name = "min_score", nullable = false)
  private Integer minScore = 60;

  // Constructors
  public OpenAnswer() {
  }

  public OpenAnswer(Question question, String answer) {
    this.question = question;
    this.answer = answer;
  }

  public OpenAnswer(Question question, String answer, Map<String, Object> rubricKeywords, Integer minScore) {
    this.question = question;
    this.answer = answer;
    this.rubricKeywords = rubricKeywords;
    this.minScore = minScore != null ? minScore : 60;
  }

  // Getters and Setters
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

  public String getAnswer() {
    return answer;
  }

  public void setAnswer(String answer) {
    this.answer = answer;
  }

  public Map<String, Object> getRubricKeywords() {
    return rubricKeywords;
  }

  public void setRubricKeywords(Map<String, Object> rubricKeywords) {
    this.rubricKeywords = rubricKeywords;
  }

  public Integer getMinScore() {
    return minScore;
  }

  public void setMinScore(Integer minScore) {
    this.minScore = minScore;
  }

  // Helper methods for rubric keywords
  @SuppressWarnings("unchecked")
  public java.util.List<String> getMustKeywords() {
    if (rubricKeywords == null)
      return java.util.Collections.emptyList();
    Object must = rubricKeywords.get("must");
    return must instanceof java.util.List ? (java.util.List<String>) must : java.util.Collections.emptyList();
  }

  @SuppressWarnings("unchecked")
  public java.util.List<String> getShouldKeywords() {
    if (rubricKeywords == null)
      return java.util.Collections.emptyList();
    Object should = rubricKeywords.get("should");
    return should instanceof java.util.List ? (java.util.List<String>) should : java.util.Collections.emptyList();
  }

  // toString, equals, hashCode
  @Override
  public String toString() {
    return "OpenAnswer{" +
        "id=" + id +
        ", question=" + (question != null ? question.getId() : null) +
        ", answer='" + answer + '\'' +
        ", minScore=" + minScore +
        '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (!(o instanceof OpenAnswer))
      return false;
    OpenAnswer that = (OpenAnswer) o;
    return java.util.Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return java.util.Objects.hash(id);
  }
}
