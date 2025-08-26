package com.bell_ringer.services;

import com.bell_ringer.config.GenerationProperties;
import com.bell_ringer.models.Question;
import com.bell_ringer.models.Question.Difficulty;
import com.bell_ringer.models.Question.Type;
import com.bell_ringer.repositories.QuestionRepository;
import com.bell_ringer.services.dto.GenerationRequest;
import com.bell_ringer.services.dto.QuestionDto;
import com.bell_ringer.services.dto.QuizGenerationResponse;
import com.bell_ringer.services.dto.ChoiceDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class QuestionService {
  private final QuestionRepository questionRepository;
  private final QuizService quizService;
  private final GenerationProperties generationProperties;
  private final CategoryService categoryService;
  private final AttemptService attemptService;

  private static final int MAX_LIMIT = 100; // hard cap to protect DB
  private static final Set<Integer> ALLOWED_LIMITS = Set.of(5, 10, 15, 20);

  public QuestionService(QuestionRepository questionRepository,
      QuizService quizService,
      GenerationProperties generationProperties,
      CategoryService categoryService,
      AttemptService attemptService) {
    this.questionRepository = questionRepository;
    this.quizService = quizService;
    this.generationProperties = generationProperties;
    this.categoryService = categoryService;
    this.attemptService = attemptService;
  }

  // ===== DTO Conversion Methods =====

  /**
   * Convert Question entity to QuestionDto without choices (for performance).
   */
  private QuestionDto convertToDtoWithoutChoices(Question question) {
    return QuestionDto.forResponseWithoutChoices(
        question.getId(),
        question.getType().name(),
        question.getCategory().getId(),
        question.getDifficulty().name(),
        question.getQuestion(),
        question.getCreatedAt(),
        question.getUpdatedAt());
  }

  /**
   * Convert Question entity to QuestionDto with choices.
   */
  private QuestionDto convertToDtoWithChoices(Question question) {
    List<ChoiceDto> choiceDtos = question.getChoices().stream()
        .map(choice -> ChoiceDto.forResponse(
            choice.getId(),
            question.getId(),
            choice.getChoiceText(),
            choice.isCorrect()))
        .toList();

    return QuestionDto.forResponse(
        question.getId(),
        question.getType().name(),
        question.getCategory().getId(),
        question.getDifficulty().name(),
        question.getQuestion(),
        choiceDtos,
        question.getCreatedAt(),
        question.getUpdatedAt());
  }

  /**
   * Convert list of Question entities to list of QuestionDtos without choices.
   */
  private List<QuestionDto> convertToDtoListWithoutChoices(List<Question> questions) {
    return questions.stream()
        .map(this::convertToDtoWithoutChoices)
        .toList();
  }

  // ===== Basic Reads =====
  @Transactional(readOnly = true)
  public Question getQuestionById(Long id) {
    if (id == null)
      throw new IllegalArgumentException("id must not be null");
    return questionRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Question not found: " + id));
  }

  @Transactional(readOnly = true)
  public QuestionDto getQuestionDtoById(Long id) {
    Question question = getQuestionById(id);
    return convertToDtoWithChoices(question);
  }

  @Transactional(readOnly = true)
  public List<Question> pickRandom(int limit) {
    int safe = normalizeLimit(limit);
    return questionRepository.pickRandom(safe);
  }

  @Transactional(readOnly = true)
  public List<Question> pickRandomFiltered(Long categoryId, Type type, Difficulty difficulty, int limit) {
    int safe = normalizeLimit(limit);
    String typeName = (type == null ? null : type.name());
    String diffName = (difficulty == null ? null : difficulty.name());
    return questionRepository.pickRandomFiltered(categoryId, typeName, diffName, safe);
  }

  // ===== Metrics & Guards =====
  @Transactional(readOnly = true)
  public long countByCategoryId(Long categoryId) {
    if (categoryId == null)
      throw new IllegalArgumentException("categoryId must not be null");
    return questionRepository.countByCategoryId(categoryId);
  }

  @Transactional(readOnly = true)
  public boolean existsByCategoryId(Long categoryId) {
    if (categoryId == null)
      throw new IllegalArgumentException("categoryId must not be null");
    return questionRepository.existsByCategoryId(categoryId);
  }

  // ===== Mode Decision & Quota =====
  @Transactional(readOnly = true)
  GenerationRequest.Mode decideMode(GenerationRequest req) {
    if (req.modeOverride() != null)
      return req.modeOverride();
    int required = generationProperties.getMinQuizzesForAdaptive();
    long completed = quizService.countCompletedByUserAndCategory(req.userId(), req.categoryId());
    return (completed >= required) ? GenerationRequest.Mode.ADAPTIVE : GenerationRequest.Mode.RANDOM;
  }

  private Quota computeQuotaInternal(GenerationRequest req) {
    int total = normalizeLimit(req.total());
    var mode = decideMode(req);
    return (mode == GenerationRequest.Mode.ADAPTIVE)
        ? adaptiveQuota(total, req.userId(), req.categoryId())
        : randomQuota(total);
  }

  @Transactional(readOnly = true)
  public QuotaDTO computeQuota(GenerationRequest req) {
    Quota q = computeQuotaInternal(req);
    return new QuotaDTO(q.easy(), q.medium(), q.hard(), q.sum());
  }

  private List<Integer> effectiveCategoryIds(Long categoryId) {
    // CategoryService returns List<Long>; convert to List<Integer> for repository
    // (IN (:categoryIds))
    List<Long> ids = categoryService.resolveSelectionIds(categoryId);
    return ids.stream().map(Long::intValue).toList();
  }

  private void assertEnoughStock(List<Integer> categoryIds, int total) {
    long stock = 0L;
    for (Integer id : categoryIds) {
      stock += countByCategoryId(Long.valueOf(id));
    }
    if (stock < total) {
      throw new IllegalArgumentException(
          "Not enough questions in these categories (have " + stock + ", need " + total + ")");
    }
  }

  @Transactional(readOnly = true)
  public List<Question> drawWithQuota(List<Integer> categoryIds, Quota quota, int total) {
    if (categoryIds == null || categoryIds.isEmpty())
      throw new IllegalArgumentException("categoryIds must not be empty");
    if (total <= 0)
      throw new IllegalArgumentException("total must be > 0");

    System.out.println("DEBUG: Drawing questions with quota: " + quota + ", total: " + total);

    // Stock guard (can be relaxed to allow partial fills)
    assertEnoughStock(categoryIds, total);

    List<Question> out = new ArrayList<>(total);
    Set<Long> seen = new HashSet<>(total * 2);

    // Overdraw factor helps reduce overlap across batches
    int over = 2;

    // EASY
    if (quota.easy() > 0) {
      var batch = questionRepository.pickRandomFilteredMany(categoryIds, null, Difficulty.EASY.name(),
          quota.easy() * over);
      System.out.println("DEBUG: Easy batch fetched " + batch.size() + " questions, need " + quota.easy());
      addUntilUnique(out, batch, quota.easy(), seen);
      System.out.println("DEBUG: After easy batch, out.size=" + out.size() + ", seen.size=" + seen.size());
    }

    // MEDIUM
    if (quota.medium() > 0) {
      var batch = questionRepository.pickRandomFilteredMany(categoryIds, null, Difficulty.MEDIUM.name(),
          quota.medium() * over);
      System.out.println("DEBUG: Medium batch fetched " + batch.size() + " questions, need " + quota.medium());
      addUntilUnique(out, batch, quota.medium(), seen);
      System.out.println("DEBUG: After medium batch, out.size=" + out.size() + ", seen.size=" + seen.size());
    }

    // HARD
    if (quota.hard() > 0) {
      var batch = questionRepository.pickRandomFilteredMany(categoryIds, null, Difficulty.HARD.name(),
          quota.hard() * over);
      System.out.println("DEBUG: Hard batch fetched " + batch.size() + " questions, need " + quota.hard());
      addUntilUnique(out, batch, quota.hard(), seen);
      System.out.println("DEBUG: After hard batch, out.size=" + out.size() + ", seen.size=" + seen.size());
    }

    // Fallback: if any bucket was short, top up with any difficulty
    int missing = total - out.size();
    if (missing > 0) {
      var topUp = questionRepository.pickRandomFilteredMany(categoryIds, null, null, missing * over);
      System.out.println("DEBUG: Top-up batch fetched " + topUp.size() + " questions, need " + missing);
      addUntilUnique(out, topUp, missing, seen);
      System.out.println("DEBUG: After top-up, out.size=" + out.size() + ", seen.size=" + seen.size());
    }

    // Final shuffle so order is random across difficulties
    Collections.shuffle(out);

    // Print final question IDs for debugging
    List<Long> questionIds = out.stream().map(Question::getId).toList();
    System.out.println("DEBUG: Final selected question IDs: " + questionIds);

    // Truncate in case we slightly overfilled (defensive)
    return out.size() > total ? out.subList(0, total) : out;
  }

  // ===== Orchestrator =====
  @Transactional
  public QuizGenerationResponse generate(GenerationRequest req) {
    if (req.userId() == null)
      throw new IllegalArgumentException("userId required");
    if (req.categoryId() == null)
      throw new IllegalArgumentException("categoryId required");
    if (req.total() <= 0)
      throw new IllegalArgumentException("total must be > 0");

    // 1) Compute difficulty split (Step 3)
    var quota = computeQuotaInternal(req);

    // 2) Draw according to quota (Step 4)
    var selected = drawWithQuota(effectiveCategoryIds(req.categoryId()), quota, req.total());

    // 3) Ensure we have a quiz to attach to (auto-create if needed)
    Long quizId = req.quizId();
    if (quizId == null) {
      var quiz = quizService.create(req.userId(), req.categoryId());
      quizId = quiz.getId();
    }

    // 4) Attach the generated questions to the quiz
    var questionIds = selected.stream().map(Question::getId).toList();
    quizService.addQuestions(quizId, questionIds);

    // 5) Create the initial attempt for this quiz
    var attempt = attemptService.startAttempt(quizId);

    // 6) Convert to DTOs (without choices for performance)
    var questionDtos = convertToDtoListWithoutChoices(selected);

    // 7) Return complete response with quiz, attempt, and questions
    return new QuizGenerationResponse(quizId, attempt.id(), questionDtos);
  }

  // ===== Helpers =====
  private int normalizeLimit(int limit) {
    if (!ALLOWED_LIMITS.contains(limit)) {
      throw new IllegalArgumentException("limit must be one of " + ALLOWED_LIMITS);
    }
    return Math.min(limit, MAX_LIMIT);
  }

  private Quota distributeByLargestRemainder(double wEasy, double wMed, double wHard, int total) {
    double sum = Math.max(1e-9, wEasy + wMed + wHard);
    double e = (wEasy / sum) * total;
    double m = (wMed / sum) * total;
    double h = (wHard / sum) * total;

    int Ei = (int) Math.floor(e);
    int Mi = (int) Math.floor(m);
    int Hi = (int) Math.floor(h);
    int remain = total - (Ei + Mi + Hi);

    double re = e - Ei, rm = m - Mi, rh = h - Hi;
    while (remain-- > 0) {
      if (re >= rm && re >= rh) {
        Ei++;
        re = -1;
      } else if (rm >= re && rm >= rh) {
        Mi++;
        rm = -1;
      } else {
        Hi++;
        rh = -1;
      }
    }
    return new Quota(Ei, Mi, Hi);
  }

  private Quota randomQuota(int total) {
    double baseE = generationProperties.getBase().getEasy();
    double baseM = generationProperties.getBase().getMedium();
    double baseH = generationProperties.getBase().getHard();
    double noise = generationProperties.getNoise();

    double e = baseE + (Math.random() - 0.5) * (noise / 2.0);
    double m = baseM + (Math.random() - 0.5) * (noise / 2.0);
    double h = baseH + (Math.random() - 0.5) * (noise / 2.0);

    return distributeByLargestRemainder(e, m, h, total);
  }

  private Quota adaptiveQuota(int total, UUID userId, Long categoryId) {
    var acc = quizService.loadAccuracy(userId, categoryId); // values in [0,1]
    double wE = 1.0 - acc.easy();
    double wM = 1.0 - acc.medium();
    double wH = 1.0 - acc.hard();

    double ws = wE + wM + wH;
    if (ws <= 1e-9) {
      wE = wM = wH = 1.0;
      ws = 3.0;
    }
    wE /= ws;
    wM /= ws;
    wH /= ws;

    double baseE = generationProperties.getBase().getEasy();
    double baseM = generationProperties.getBase().getMedium();
    double baseH = generationProperties.getBase().getHard();
    double alpha = generationProperties.getAdaptiveAlpha();

    double e = alpha * baseE + (1.0 - alpha) * wE;
    double m = alpha * baseM + (1.0 - alpha) * wM;
    double h = alpha * baseH + (1.0 - alpha) * wH;

    return distributeByLargestRemainder(e, m, h, total);
  }

  private int addUntilUnique(List<Question> target,
      List<Question> batch,
      int need,
      Set<Long> seen) {
    int added = 0;
    System.out.println("DEBUG: addUntilUnique called with batch.size=" + batch.size() + ", need=" + need
        + ", seen.size=" + seen.size());
    for (Question q : batch) {
      if (added >= need)
        break;
      if (seen.add(q.getId())) {
        target.add(q);
        added++;
        System.out.println("DEBUG: Added question ID " + q.getId() + " (added=" + added + "/" + need + ")");
      } else {
        System.out.println("DEBUG: Skipped duplicate question ID " + q.getId());
      }
    }
    System.out.println("DEBUG: addUntilUnique completed, added=" + added + ", final seen.size=" + seen.size());
    return added;
  }

  // ===== DTOs =====
  record Quota(int easy, int medium, int hard) {
    int sum() {
      return easy + medium + hard;
    }
  }

  public static record QuotaDTO(int easy, int medium, int hard, int sum) {
  }
}
