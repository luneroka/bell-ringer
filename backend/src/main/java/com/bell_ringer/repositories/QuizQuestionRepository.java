package com.bell_ringer.repositories;

import com.bell_ringer.models.QuizQuestion;
import com.bell_ringer.models.id.QuizQuestionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface QuizQuestionRepository extends JpaRepository<QuizQuestion, QuizQuestionId> {
  // List all links for a quiz
  List<QuizQuestion> findAllByQuizId(Long quizId);

  // Does this quiz already contain this question?
  boolean existsByQuizIdAndQuestionId(Long quizId, Long questionId);

  // Count how many questions are attached to a quiz
  long countByQuizId(Long quizId);

  // Remove a single link
  @Transactional
  long deleteByQuizIdAndQuestionId(Long quizId, Long questionId);

  // Remove all links for a quiz (e.g., when regenerating)
  @Transactional
  long deleteByQuizId(Long quizId);
}
