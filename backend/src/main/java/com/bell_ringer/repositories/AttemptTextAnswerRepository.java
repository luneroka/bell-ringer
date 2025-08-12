package com.bell_ringer.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.bell_ringer.models.AttemptTextAnswer;
import com.bell_ringer.models.id.AttemptTextAnswerId;

import java.util.List;
import java.util.Optional;

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
}
