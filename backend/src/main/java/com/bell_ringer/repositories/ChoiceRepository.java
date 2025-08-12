package com.bell_ringer.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bell_ringer.models.Choice;

public interface ChoiceRepository extends JpaRepository<Choice, Long> {
  List<Choice> findByQuestionId(Long questionId);

  boolean existsByIdAndIsCorrectTrue(Long choiceId);

  List<Choice> findByQuestionIdAndIsCorrectTrue(Long questionId);

}
