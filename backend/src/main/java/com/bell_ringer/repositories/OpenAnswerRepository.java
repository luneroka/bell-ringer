package com.bell_ringer.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bell_ringer.models.OpenAnswer;

public interface OpenAnswerRepository extends JpaRepository<OpenAnswer, Long> {
  List<OpenAnswer> findByQuestionId(Long questionId);

  Optional<OpenAnswer> findByQuestionIdAndAnswerIgnoreCase(Long questionId, String answer);

  boolean existsByQuestionIdAndAnswerIgnoreCase(Long questionId, String answer);
}
