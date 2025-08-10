package com.bell_ringer.models;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "quizzes", indexes = {
    @Index(name = "idx_quizzes_user", columnList = "user_id"),
    @Index(name = "idx_quizzes_category", columnList = "category_id")
})
public class Quiz {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "user_id", nullable = false, columnDefinition = "uuid")
  private UUID userId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "category_id")
  private Category category; // sub-category in your terms

  @Column(name = "date_created", nullable = false)
  private OffsetDateTime dateCreated = OffsetDateTime.now();

  @Column(name = "completed_at")
  private OffsetDateTime completedAt;

  public Quiz() {}

  public Quiz(UUID userId, Category category) {
    this.userId = userId;
    this.category = category;
  }

  // getters/setters
  public Long getId() { return id; }
  public UUID getUserId() { return userId; }
  public void setUserId(UUID userId) { this.userId = userId; }
  public Category getCategory() { return category; }
  public void setCategory(Category category) { this.category = category; }
  public OffsetDateTime getDateCreated() { return dateCreated; }
  public void setDateCreated(OffsetDateTime dateCreated) { this.dateCreated = dateCreated; }
  public OffsetDateTime getCompletedAt() { return completedAt; }
  public void setCompletedAt(OffsetDateTime completedAt) { this.completedAt = completedAt; }
}