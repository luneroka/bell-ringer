package com.bell_ringer.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.bell_ringer.models.AttemptSelectedChoice;
import com.bell_ringer.models.id.AttemptSelectedChoiceId;

import java.util.List;

public interface AttemptSelectedChoiceRepository extends JpaRepository<AttemptSelectedChoice, AttemptSelectedChoiceId> {

  /**
   * Find all selected choices for a specific attempt
   */
  @Query("SELECT asc FROM AttemptSelectedChoice asc WHERE asc.attemptId = :attemptId ORDER BY asc.questionId, asc.choiceId")
  List<AttemptSelectedChoice> findByAttemptId(@Param("attemptId") Long attemptId);

  /**
   * Find all selected choices for a specific attempt and question
   */
  @Query("SELECT asc FROM AttemptSelectedChoice asc WHERE asc.attemptId = :attemptId AND asc.questionId = :questionId ORDER BY asc.choiceId")
  List<AttemptSelectedChoice> findByAttemptIdAndQuestionId(@Param("attemptId") Long attemptId,
      @Param("questionId") Long questionId);

  /**
   * Count selected choices for an attempt
   */
  @Query("SELECT COUNT(asc) FROM AttemptSelectedChoice asc WHERE asc.attemptId = :attemptId")
  long countByAttemptId(@Param("attemptId") Long attemptId);

  /**
   * Delete all selected choices for an attempt (cleanup)
   */
  void deleteByAttemptId(Long attemptId);
}
