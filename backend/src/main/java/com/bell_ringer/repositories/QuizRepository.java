package com.bell_ringer.repositories;

import com.bell_ringer.models.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface QuizRepository extends JpaRepository<Quiz, Long> {

  // 2.B — count completed quizzes by user & category
  @Query(value = """
        SELECT COUNT(*)
          FROM quizzes q
         WHERE q.user_id = :userId
           AND q.category_id = :categoryId
           AND q.completed_at IS NOT NULL
      """, nativeQuery = true)
  long countCompletedByUserAndCategoryWithCompletedAt(@Param("userId") UUID userId,
      @Param("categoryId") Long categoryId);

  // 2.C — accuracy per difficulty for this user/category
  @Query(value = """
        WITH answer_results AS (
          -- Multiple choice answers
          SELECT qu.difficulty,
                 CASE WHEN ac.choice_id IN (
                   SELECT c.id FROM choices c WHERE c.question_id = qu.id AND c.is_correct = true
                 ) THEN 1 ELSE 0 END AS answered_correctly
            FROM quizzes qz
            JOIN attempts a ON a.quiz_id = qz.id
            JOIN attempt_selected_choices ac ON ac.attempt_id = a.id
            JOIN questions qu ON qu.id = ac.question_id
           WHERE qz.user_id = :userId
             AND qu.category_id = :categoryId
             AND qz.completed_at IS NOT NULL

          UNION ALL

          -- Text answers
          SELECT qu.difficulty,
                 CASE WHEN ata.is_correct = true THEN 1 ELSE 0 END AS answered_correctly
            FROM quizzes qz
            JOIN attempts a ON a.quiz_id = qz.id
            JOIN attempt_text_answers ata ON ata.attempt_id = a.id
            JOIN questions qu ON qu.id = ata.question_id
           WHERE qz.user_id = :userId
             AND qu.category_id = :categoryId
             AND qz.completed_at IS NOT NULL
             AND ata.is_correct IS NOT NULL
        )
        SELECT difficulty,
               SUM(answered_correctly) AS correct,
               COUNT(*) AS total
          FROM answer_results
         WHERE difficulty IS NOT NULL
         GROUP BY difficulty
      """, nativeQuery = true)
  List<DifficultyStatsRow> findAccuracyByUserAndCategory(@Param("userId") UUID userId,
      @Param("categoryId") Long categoryId);

  interface DifficultyStatsRow {
    String getDifficulty(); // e.g. "EASY" | "MEDIUM" | "HARD"

    long getCorrect();

    long getTotal();
  }
}