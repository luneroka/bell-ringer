package com.bell_ringer.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.OffsetDateTime;

@Entity
@Table(
    name= "questions",
    indexes = {
        @Index(name = "idx_questions_category", columnList = "category_id"),
        @Index(name = "idx_questions_type", columnList = "type"),
        @Index(name = "idx_questions_difficulty", columnList="difficulty")
    }
)
public class Question {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 50)
  private Type type;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 50)
  private Difficulty difficulty;

  @NotBlank
  @Column(name = "question", nullable = false, columnDefinition = "TEXT")
  private String question;

  @JsonIgnore
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "category_id", nullable = false)
  private Category category;

  @OneToMany(mappedBy = "question", fetch = FetchType.LAZY)
  private java.util.Set<QuizQuestion> quizLinks = new java.util.LinkedHashSet<>();

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
  public Question() {}

  // Convenience constructor
  public Question(Type type, Difficulty difficulty, String question, Category category) {
    this.type = type;
    this.difficulty = difficulty;
    this.question = question;
    this.category = category;
  }


  // Getters and setters
  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }
  public Type getType() { return type; }
  public void setType(Type type) { this.type = type; }
  public Difficulty getDifficulty() { return difficulty; }
  public void setDifficulty(Difficulty difficulty) { this.difficulty = difficulty; }
  public String getQuestion() { return question; }
  public void setQuestion(String question) { this.question = question; }
  public Category getCategory() { return category; }
  public void setCategory(Category category) { this.category = category; }
}
