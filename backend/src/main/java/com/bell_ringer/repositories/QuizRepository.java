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
    SELECT qu.difficulty        AS difficulty,
           SUM(CASE WHEN aa.answered_correctly THEN 1 ELSE 0 END) AS correct,
           COUNT(*)             AS total
      FROM quizzes qz
      JOIN attempts a         ON a.quiz_id = qz.id
      JOIN attempt_answers aa ON aa.attempt_id = a.id
      JOIN questions qu       ON qu.id = aa.question_id
     WHERE qz.user_id = :userId
       AND qu.category_id = :categoryId
  GROUP BY qu.difficulty
  """, nativeQuery = true)
  List<DifficultyStatsRow> findAccuracyByUserAndCategory(@Param("userId") UUID userId,
                                                         @Param("categoryId") Long categoryId);

  interface DifficultyStatsRow {
    String getDifficulty(); // e.g. "EASY" | "MEDIUM" | "HARD"
    long getCorrect();
    long getTotal();
  }
}