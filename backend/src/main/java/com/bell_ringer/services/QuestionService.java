package com.bell_ringer.services;

import com.bell_ringer.config.GenerationProperties;
import com.bell_ringer.models.Question;
import com.bell_ringer.models.Question.Type;
import com.bell_ringer.models.Question.Difficulty;
import com.bell_ringer.repositories.QuestionRepository;
import com.bell_ringer.services.QuizService;
import com.bell_ringer.services.dto.GenerationRequest;
import com.bell_ringer.services.dto.GenerationRequest.Mode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class QuestionService {
  private final QuestionRepository questionRepository;
  private final QuizService quizService;
  private final GenerationProperties generationProperties;

  public QuestionService(QuestionRepository questionRepository, QuizService quizService, GenerationProperties generationProperties) {
    this.questionRepository = questionRepository;
    this.quizService = quizService;
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

  //  ***** DECIDE MODE ***** //
  @Transactional(readOnly = true)
  Mode decideMode(GenerationRequest req) {
    // 1. Respect explicit override if provided
    if (req.modeOverride() != null) return req.modeOverride();

    // 2. Otherwise, switch when the user has enough history in the sub_category
    int required = generationProperties.getMinQuizzesForAdaptive();
    long completed = quizService.countCompletedByUserAndCategory(req.userId(), req.categoryId());

    return (completed >= required) ? Mode.ADAPTIVE : Mode.RANDOM;
  }

  @Transactional(readOnly = true)
  public Quota computeQuota(GenerationRequest req) {
    int total = normalizeLimit(req.total()); // or just req.total() if you already validated
    Mode mode = decideMode(req);
    return (mode == Mode.ADAPTIVE)
        ? adaptiveQuota(total, req.userId(), req.categoryId())
        : randomQuota(total);
  }

  // For tests / logs
  private void assertQuotaSums(Quota q, int total) {
    if (q.sum() != total) {
      throw new IllegalStateException("Quota sum mismatch: " + q.sum() + " != " + total);
    }
  }

  /** How many questions to draw per difficulty. */
  record Quota(int easy, int medium, int hard) {
    int sum() { return easy + medium + hard; }
  }

  // ***** HELPERS ***** //
  private int normalizeLimit(int limit) {
    if (limit <= 0) return 1;
    return Math.min(limit, 1000);
  }

  private Quota distributeByLargestRemainder(double wEasy, double wMed, double wHard, int total) {
    // avoid division by zero
    double sum = Math.max(1e-9, wEasy + wMed + wHard);
    double e = (wEasy / sum) * total;
    double m = (wMed  / sum) * total;
    double h = (wHard / sum) * total;

    int Ei = (int) Math.floor(e);
    int Mi = (int) Math.floor(m);
    int Hi = (int) Math.floor(h);
    int remain = total - (Ei + Mi + Hi);

    double re = e - Ei, rm = m - Mi, rh = h - Hi;
    while (remain-- > 0) {
      if (re >= rm && re >= rh) { Ei++; re = -1; }
      else if (rm >= re && rm >= rh) { Mi++; rm = -1; }
      else { Hi++; rh = -1; }
    }
    return new Quota(Ei, Mi, Hi);
  }

  private Quota randomQuota(int total) {
    double baseE = generationProperties.getBase().getEasy();
    double baseM = generationProperties.getBase().getMedium();
    double baseH = generationProperties.getBase().getHard();
    double noise = generationProperties.getNoise(); // e.g. 0.10

    // add a small jitter of ±noise/2 to each bucket
    double e = baseE + (Math.random() - 0.5) * (noise / 2.0);
    double m = baseM + (Math.random() - 0.5) * (noise / 2.0);
    double h = baseH + (Math.random() - 0.5) * (noise / 2.0);

    return distributeByLargestRemainder(e, m, h, total);
  }

  private Quota adaptiveQuota(int total, java.util.UUID userId, Long categoryId) {
    var acc = quizService.loadAccuracy(userId, categoryId); // values in [0,1]
    double wE = 1.0 - acc.easy();
    double wM = 1.0 - acc.medium();
    double wH = 1.0 - acc.hard();

    // normalize weakness weights (if all 0, give equal weight)
    double ws = wE + wM + wH;
    if (ws <= 1e-9) { wE = wM = wH = 1.0; ws = 3.0; }
    wE /= ws; wM /= ws; wH /= ws;

    double baseE = generationProperties.getBase().getEasy();
    double baseM = generationProperties.getBase().getMedium();
    double baseH = generationProperties.getBase().getHard();
    double alpha = generationProperties.getAdaptiveAlpha(); // e.g. 0.6

    // mix base with user weaknesses
    double e = alpha * baseE + (1.0 - alpha) * wE;
    double m = alpha * baseM + (1.0 - alpha) * wM;
    double h = alpha * baseH + (1.0 - alpha) * wH;

    return distributeByLargestRemainder(e, m, h, total);
  }
}
