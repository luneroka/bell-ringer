package com.bell_ringer.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.bell_ringer.models.AttemptTextAnswer;
import com.bell_ringer.models.id.AttemptTextAnswerId;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AttemptTextAnswerRepository extends JpaRepository<AttemptTextAnswer, AttemptTextAnswerId> {

  /**
   * Find all text answers for a specific attempt
   */
  @Query("SELECT ata FROM AttemptTextAnswer ata WHERE ata.attemptId = :attemptId ORDER BY ata.questionId")
  List<AttemptTextAnswer> findByAttemptId(@Param("attemptId") Long attemptId);

  /**
   * Find text answer for a specific attempt and question
   */
  @Query("SELECT ata FROM AttemptTextAnswer ata WHERE ata.attemptId = :attemptId AND ata.questionId = :questionId")
  Optional<AttemptTextAnswer> findByAttemptIdAndQuestionId(@Param("attemptId") Long attemptId,
      @Param("questionId") Long questionId);

  /**
   * Find unscored text answers (for grading)
   */
  @Query("SELECT ata FROM AttemptTextAnswer ata WHERE ata.score IS NULL ORDER BY ata.answeredAt")
  List<AttemptTextAnswer> findUnscored();

  /**
   * Find unscored text answers for a specific attempt
   */
  @Query("SELECT ata FROM AttemptTextAnswer ata WHERE ata.attemptId = :attemptId AND ata.score IS NULL ORDER BY ata.questionId")
  List<AttemptTextAnswer> findUnscoredByAttemptId(@Param("attemptId") Long attemptId);

  /**
   * Count text answers for an attempt
   */
  @Query("SELECT COUNT(ata) FROM AttemptTextAnswer ata WHERE ata.attemptId = :attemptId")
  long countByAttemptId(@Param("attemptId") Long attemptId);

  /**
   * Delete all text answers for an attempt (cleanup)
   */
  void deleteByAttemptId(Long attemptId);

  /**
   * Count correct text answers for a specific user across all completed attempts
   */
  @Query("SELECT COUNT(ata) FROM AttemptTextAnswer ata " +
      "JOIN ata.attempt a " +
      "WHERE ata.quiz.userId = :userId " +
      "AND a.completedAt IS NOT NULL " +
      "AND ata.isCorrect = true")
  long countCorrectTextAnswersByUserId(@Param("userId") UUID userId);

  /**
   * Count total text answers for a specific user across all completed attempts
   */
  @Query("SELECT COUNT(ata) FROM AttemptTextAnswer ata " +
      "JOIN ata.attempt a " +
      "WHERE ata.quiz.userId = :userId " +
      "AND a.completedAt IS NOT NULL")
  long countTotalTextAnswersByUserId(@Param("userId") UUID userId);

  /**
   * Count correct text answers for a specific attempt
   */
  @Query("SELECT COUNT(ata) FROM AttemptTextAnswer ata " +
      "WHERE ata.attemptId = :attemptId " +
      "AND ata.isCorrect = true")
  long countCorrectTextAnswersByAttemptId(@Param("attemptId") Long attemptId);
}
