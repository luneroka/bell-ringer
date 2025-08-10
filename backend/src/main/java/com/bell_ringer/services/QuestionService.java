package com.bell_ringer.services;

import com.bell_ringer.config.GenerationProperties;
import com.bell_ringer.models.Question;
import com.bell_ringer.models.Question.Type;
import com.bell_ringer.models.Question.Difficulty;
import com.bell_ringer.repositories.QuestionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class QuestionService {
  private final QuestionRepository questionRepository;
  private final GenerationProperties generationProperties;

  public QuestionService(QuestionRepository questionRepository, GenerationProperties generationProperties) {
    this.questionRepository = questionRepository;
    this.generationProperties = generationProperties;
  }

  // GET QUESTION BY ID
  @Transactional(readOnly = true)
  public Question getQuestionById(Long id) {
    if (id == null) throw new IllegalArgumentException("id must not be null");
    return questionRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Question not found: " + id));
  }

  // ***** RANDOM SELECTION ***** //

  @Transactional(readOnly = true)
  public List<Question> pickRandom(int limit) {
    int safe = normalizeLimit(limit);
    return questionRepository.pickRandom(safe);
  }

  @Transactional(readOnly = true)
  public List<Question> pickRandomFiltered(Long categoryId, Type type, Difficulty difficulty, int limit) {
    int safe = normalizeLimit(limit);
    String typeName = type == null ? null : type.name();
    String diffName = difficulty == null ? null : difficulty.name();
    return questionRepository.pickRandomFiltered(categoryId, typeName, diffName, safe);
  }

  // ***** METRICS & GUARDS ***** //

  @Transactional(readOnly = true)
  public long countByCategoryId(Long categoryId) {
    if (categoryId == null) throw new IllegalArgumentException("categoryId must not be null");
    return questionRepository.countByCategoryId(categoryId);
  }

  @Transactional(readOnly = true)
  public boolean existsByCategoryId(Long categoryId) {
    if (categoryId == null) throw new IllegalArgumentException("categoryId must not be null");
    return questionRepository.existsByCategoryId(categoryId);
  }

  // ***** HELPERS ***** //
  private int normalizeLimit(int limit) {
    if (limit <= 0) return 1;
    return Math.min(limit, 1000);
  }
}
