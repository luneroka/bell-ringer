package com.bell_ringer.services;

import com.bell_ringer.models.Category;
import com.bell_ringer.models.Question;
import com.bell_ringer.models.Question.Type;
import com.bell_ringer.models.Question.Difficulty;
import com.bell_ringer.repositories.QuestionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

@Service
public class QuestionService {
  private final QuestionRepository questionRepository;

  public QuestionService(QuestionRepository questionRepository) {
    this.questionRepository = questionRepository;
  }

  // ***** QUESTION SERVICES ***** //

  // GET ALL QUESTIONS (paged)
  @Transactional(readOnly = true)
  public Page<Question> getAllQuestions(Pageable pageable) {
    return questionRepository.findAll(normalize(pageable));
  }

  // Convenience: GET ALL without paging
  @Transactional(readOnly = true)
  public Iterable<Question> getAllQuestions() {
    return questionRepository.findAll();
  }

  // GET QUESTION BY ID
  @Transactional(readOnly = true)
  public Question getQuestionById(Long id) {
    return questionRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Question not found: " + id));
  }

  // GET QUESTIONS BY TYPE (paged)
  @Transactional(readOnly = true)
  public Page<Question> getQuestionsByType(Type type, Pageable pageable) {
    if (type == null) throw new IllegalArgumentException("type must not be null");
    return questionRepository.findAllByType(type, normalize(pageable));
  }

  // Convenience: BY TYPE without paging
  @Transactional(readOnly = true)
  public Iterable<Question> getQuestionsByType(Type type) {
    return getQuestionsByType(type, Pageable.unpaged()).getContent();
  }

  // GET QUESTIONS BY DIFFICULTY (paged)
  @Transactional(readOnly = true)
  public Page<Question> getQuestionsByDifficulty(Difficulty difficulty, Pageable pageable) {
    if (difficulty == null) throw new IllegalArgumentException("difficulty must not be null");
    return questionRepository.findAllByDifficulty(difficulty, normalize(pageable));
  }

  // Convenience: BY DIFFICULTY without paging
  @Transactional(readOnly = true)
  public Iterable<Question> getQuestionsByDifficulty(Difficulty difficulty) {
    return getQuestionsByDifficulty(difficulty, Pageable.unpaged()).getContent();
  }

  // GET QUESTIONS BY CATEGORY (paged)
  @Transactional(readOnly = true)
  public Page<Question> getQuestionsByCategory(Category category, Pageable pageable) {
    if (category == null) throw new IllegalArgumentException("category must not be null");
    return questionRepository.findAllByCategory(category, normalize(pageable));
  }

  // Convenience: BY CATEGORY without paging
  @Transactional(readOnly = true)
  public Iterable<Question> getQuestionsByCategory(Category category) {
    return getQuestionsByCategory(category, Pageable.unpaged()).getContent();
  }

  // GET QUESTIONS BY CATEGORY ID (paged)
  @Transactional(readOnly = true)
  public Page<Question> getQuestionsByCategoryId(Long categoryId, Pageable pageable) {
    if (categoryId == null) throw new IllegalArgumentException("categoryId must not be null");
    return questionRepository.findAllByCategoryId(categoryId, normalize(pageable));
  }

  // Convenience: BY CATEGORY ID without paging
  @Transactional(readOnly = true)
  public Iterable<Question> getQuestionsByCategoryId(Long categoryId) {
    return getQuestionsByCategoryId(categoryId, Pageable.unpaged()).getContent();
  }

  // ***** COMBINED FILTERS ***** //

  // GET QUESTIONS BY TYPE AND DIFFICULTY (paged)
  @Transactional(readOnly = true)
  public Page<Question> getQuestionsByTypeAndDifficulty(Type type, Difficulty difficulty, Pageable pageable) {
    if (type == null) throw new IllegalArgumentException("type must not be null");
    if (difficulty == null) throw new IllegalArgumentException("difficulty must not be null");
    return questionRepository.findAllByTypeAndDifficulty(type, difficulty, normalize(pageable));
  }

  // Convenience: BY TYPE AND DIFFICULTY without paging
  @Transactional(readOnly = true)
  public Iterable<Question> getQuestionsByTypeAndDifficulty(Type type, Difficulty difficulty) {
    return getQuestionsByTypeAndDifficulty(type, difficulty, Pageable.unpaged()).getContent();
  }

  // GET QUESTIONS BY CATEGORY ID AND TYPE (paged)
  @Transactional(readOnly = true)
  public Page<Question> getQuestionsByCategoryIdAndType(Long categoryId, Type type, Pageable pageable) {
    if (categoryId == null) throw new IllegalArgumentException("categoryId must not be null");
    if (type == null) throw new IllegalArgumentException("type must not be null");
    return questionRepository.findAllByCategoryIdAndType(categoryId, type, normalize(pageable));
  }

  // Convenience: BY CATEGORY ID AND TYPE without paging
  @Transactional(readOnly = true)
  public Iterable<Question> getQuestionsByCategoryIdAndType(Long categoryId, Type type) {
    return getQuestionsByCategoryIdAndType(categoryId, type, Pageable.unpaged()).getContent();
  }

  // GET QUESTIONS BY CATEGORY ID AND DIFFICULTY (paged)
  @Transactional(readOnly = true)
  public Page<Question> getQuestionsByCategoryIdAndDifficulty(Long categoryId, Difficulty difficulty, Pageable pageable) {
    if (categoryId == null) throw new IllegalArgumentException("categoryId must not be null");
    if (difficulty == null) throw new IllegalArgumentException("difficulty must not be null");
    return questionRepository.findAllByCategoryIdAndDifficulty(categoryId, difficulty, normalize(pageable));
  }

  // Convenience: BY CATEGORY ID AND DIFFICULTY without paging
  @Transactional(readOnly = true)
  public Iterable<Question> getQuestionsByCategoryIdAndDifficulty(Long categoryId, Difficulty difficulty) {
    return getQuestionsByCategoryIdAndDifficulty(categoryId, difficulty, Pageable.unpaged()).getContent();
  }

  // GET QUESTIONS BY CATEGORY ID AND TYPE AND DIFFICULTY (paged)
  @Transactional(readOnly = true)
  public Page<Question> getQuestionsByCategoryIdAndTypeAndDifficulty(Long categoryId, Type type, Difficulty difficulty, Pageable pageable) {
    if (categoryId == null) throw new IllegalArgumentException("categoryId must not be null");
    if (type == null) throw new IllegalArgumentException("type must not be null");
    if (difficulty == null) throw new IllegalArgumentException("difficulty must not be null");
    return questionRepository.findAllByCategoryIdAndTypeAndDifficulty(categoryId, type, difficulty, normalize(pageable));
  }

  // Convenience: BY CATEGORY ID AND TYPE AND DIFFICULTY without paging
  @Transactional(readOnly = true)
  public Iterable<Question> getQuestionsByCategoryIdAndTypeAndDifficulty(Long categoryId, Type type, Difficulty difficulty) {
    return getQuestionsByCategoryIdAndTypeAndDifficulty(categoryId, type, difficulty, Pageable.unpaged()).getContent();
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

  // ----- HELPERS -----
  private Pageable normalize(Pageable pageable) {
    return pageable == null ? Pageable.unpaged() : pageable;
  }

  private int normalizeLimit(int limit) {
    if (limit <= 0) return 1;
    return Math.min(limit, 1000); // hard cap to protect DB
  }
}
