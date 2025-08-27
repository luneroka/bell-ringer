package com.bell_ringer.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.bell_ringer.models.AttemptSelectedChoice;
import com.bell_ringer.models.id.AttemptSelectedChoiceId;

import java.util.List;
import java.util.UUID;

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

    /**
     * Count correct multiple choice answers for a specific user across all
     * completed attempts
     * Note: Counts distinct questions where at least one correct choice was
     * selected
     */
    @Query("SELECT COUNT(DISTINCT asc.questionId) FROM AttemptSelectedChoice asc " +
            "JOIN asc.choice c " +
            "JOIN asc.attempt a " +
            "WHERE asc.quiz.userId = :userId " +
            "AND a.completedAt IS NOT NULL " +
            "AND c.isCorrect = true")
    long countCorrectChoicesByUserId(@Param("userId") UUID userId);

    /**
     * Count total multiple choice answers for a specific user across all completed
     * attempts
     * Note: Counts distinct questions answered
     */
    @Query("SELECT COUNT(DISTINCT asc.questionId) FROM AttemptSelectedChoice asc " +
            "JOIN asc.attempt a " +
            "WHERE asc.quiz.userId = :userId " +
            "AND a.completedAt IS NOT NULL")
    long countTotalChoicesByUserId(@Param("userId") UUID userId);

    /**
     * Count correct multiple choice answers for a specific attempt
     * Note: Counts distinct questions where at least one correct choice was
     * selected
     */
    @Query("SELECT COUNT(DISTINCT asc.questionId) FROM AttemptSelectedChoice asc " +
            "JOIN asc.choice c " +
            "WHERE asc.attemptId = :attemptId " +
            "AND c.isCorrect = true")
    long countCorrectChoicesByAttemptId(@Param("attemptId") Long attemptId);
}
