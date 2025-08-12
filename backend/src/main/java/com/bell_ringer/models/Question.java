package com.bell_ringer.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "questions", indexes = {
    @Index(name = "idx_questions_category", columnList = "category_id"),
    @Index(name = "idx_questions_type", columnList = "type"),
    @Index(name = "idx_questions_difficulty", columnList = "difficulty")
})
public class Question {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 50)
  private Type type;

  @JsonIgnore
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "category_id", nullable = false)
  private Category category;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 50)
  private Difficulty difficulty;

  @NotBlank
  @Column(name = "question", nullable = false, columnDefinition = "TEXT")
  private String question;

  @OneToMany(mappedBy = "question", fetch = FetchType.LAZY)
  private java.util.Set<QuizQuestion> quizLinks = new java.util.LinkedHashSet<>();

  @OneToMany(mappedBy = "question", fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST,
      CascadeType.MERGE }, orphanRemoval = true)
  private List<Choice> choices = new ArrayList<>();

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private OffsetDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private OffsetDateTime updatedAt;

  public enum Type {
    UNIQUE_CHOICE,
    MULTIPLE_CHOICE,
    TRUE_FALSE,
    SHORT_ANSWER
  }

  public enum Difficulty {
    EASY,
    MEDIUM,
    HARD
  }

  // Default constructor
  public Question() {
  }

  // Convenience constructor
  public Question(Type type, Difficulty difficulty, String question, Category category) {
    this.type = type;
    this.difficulty = difficulty;
    this.question = question;
    this.category = category;
  }

  // Helpers to keep both sides in sync
  public void addChoice(Choice choice) {
    choice.setQuestion(this);
    this.choices.add(choice);
  }

  public void removeChoice(Choice choice) {
    this.choices.remove(choice);
    choice.setQuestion(null);
  }

  // Getters and setters
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Type getType() {
    return type;
  }

  public void setType(Type type) {
    this.type = type;
  }

  public Difficulty getDifficulty() {
    return difficulty;
  }

  public void setDifficulty(Difficulty difficulty) {
    this.difficulty = difficulty;
  }

  public String getQuestion() {
    return question;
  }

  public void setQuestion(String question) {
    this.question = question;
  }

  public Category getCategory() {
    return category;
  }

  public void setCategory(Category category) {
    this.category = category;
  }

  public java.util.List<Choice> getChoices() {
    return choices;
  }

  public void setChoices(java.util.List<Choice> choices) {
    this.choices.clear();
    if (choices != null) {
      for (Choice c : choices)
        addChoice(c);
    }
  }

  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(OffsetDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public OffsetDateTime getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(OffsetDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }
}
