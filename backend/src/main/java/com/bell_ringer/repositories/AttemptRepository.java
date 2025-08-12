package com.bell_ringer.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.bell_ringer.models.Attempt;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AttemptRepository extends JpaRepository<Attempt, Long> {

  /**
   * Find all attempts for a specific quiz
   */
  @Query("SELECT a FROM Attempt a WHERE a.quiz.id = :quizId ORDER BY a.startedAt DESC")
  List<Attempt> findByQuizId(@Param("quizId") Long quizId);

  /**
   * Find all attempts for a specific user (via quiz)
   */
  @Query("SELECT a FROM Attempt a WHERE a.quiz.userId = :userId ORDER BY a.startedAt DESC")
  List<Attempt> findByUserId(@Param("userId") UUID userId);

  /**
   * Find all attempts for a user in a specific category
   */
  @Query("SELECT a FROM Attempt a WHERE a.quiz.userId = :userId AND a.quiz.category.id = :categoryId ORDER BY a.startedAt DESC")
  List<Attempt> findByUserIdAndCategoryId(@Param("userId") UUID userId, @Param("categoryId") Long categoryId);

  /**
   * Find the most recent attempt for a quiz
   */
  @Query("SELECT a FROM Attempt a WHERE a.quiz.id = :quizId ORDER BY a.startedAt DESC LIMIT 1")
  Optional<Attempt> findMostRecentByQuizId(@Param("quizId") Long quizId);

  /**
   * Find completed attempts for a user
   */
  @Query("SELECT a FROM Attempt a WHERE a.quiz.userId = :userId AND a.completedAt IS NOT NULL ORDER BY a.completedAt DESC")
  List<Attempt> findCompletedByUserId(@Param("userId") UUID userId);

  /**
   * Find incomplete attempts for a user
   */
  @Query("SELECT a FROM Attempt a WHERE a.quiz.userId = :userId AND a.completedAt IS NULL ORDER BY a.startedAt DESC")
  List<Attempt> findIncompleteByUserId(@Param("userId") UUID userId);

  /**
   * Count total attempts for a user
   */
  @Query("SELECT COUNT(a) FROM Attempt a WHERE a.quiz.userId = :userId")
  long countByUserId(@Param("userId") UUID userId);

  /**
   * Count completed attempts for a user
   */
  @Query("SELECT COUNT(a) FROM Attempt a WHERE a.quiz.userId = :userId AND a.completedAt IS NOT NULL")
  long countCompletedByUserId(@Param("userId") UUID userId);
}
