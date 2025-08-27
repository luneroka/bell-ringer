package com.bell_ringer.services;

import com.bell_ringer.models.Attempt;
import com.bell_ringer.models.AttemptSelectedChoice;
import com.bell_ringer.models.AttemptTextAnswer;
import com.bell_ringer.models.Quiz;
import com.bell_ringer.repositories.AttemptRepository;
import com.bell_ringer.repositories.AttemptSelectedChoiceRepository;
import com.bell_ringer.repositories.AttemptTextAnswerRepository;
import com.bell_ringer.services.dto.AttemptDto;
import com.bell_ringer.services.dto.AttemptRequest;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class AttemptService {

  private final AttemptRepository attempts;
  private final AttemptSelectedChoiceRepository selectedChoices;
  private final AttemptTextAnswerRepository textAnswers;
  private final QuizService quizService;
  private final AttemptSelectedChoiceService selectedChoiceService;

  public AttemptService(AttemptRepository attempts,
      AttemptSelectedChoiceRepository selectedChoices,
      AttemptTextAnswerRepository textAnswers,
      QuizService quizService,
      AttemptSelectedChoiceService selectedChoiceService) {
    this.attempts = attempts;
    this.selectedChoices = selectedChoices;
    this.textAnswers = textAnswers;
    this.quizService = quizService;
    this.selectedChoiceService = selectedChoiceService;
  }

  // ===== DTO Conversion Methods =====

  /**
   * Convert Attempt entity to AttemptDto without answer details (basic view).
   */
  private AttemptDto convertToDto(Attempt attempt) {
    return AttemptDto.forResponse(
        attempt.getId(),
        attempt.getQuiz().getId(),
        attempt.getStartedAt(),
        attempt.getCompletedAt());
  }

  /**
   * Convert Attempt entity to AttemptDto with answer details included.
   */
  private AttemptDto convertToDtoWithAnswers(Attempt attempt) {
    // Load answer details
    List<AttemptSelectedChoice> choices = selectedChoices.findByAttemptId(attempt.getId());
    List<AttemptTextAnswer> texts = textAnswers.findByAttemptId(attempt.getId());

    // Convert to DTOs
    List<AttemptDto.AttemptSelectedChoiceDto> choiceDtos = choices.stream()
        .map(choice -> AttemptDto.AttemptSelectedChoiceDto.from(
            choice.getAttemptId(),
            choice.getQuestionId(),
            choice.getChoiceId(),
            choice.getSelectedAt()))
        .collect(Collectors.toList());

    List<AttemptDto.AttemptTextAnswerDto> textDtos = texts.stream()
        .map(text -> AttemptDto.AttemptTextAnswerDto.from(
            text.getAttemptId(),
            text.getQuestionId(),
            text.getAnswerText(),
            text.getScore(),
            text.getIsCorrect(),
            text.getFeedback(),
            text.getAnsweredAt()))
        .collect(Collectors.toList());

    return AttemptDto.forResponseWithAnswers(
        attempt.getId(),
        attempt.getQuiz().getId(),
        attempt.getStartedAt(),
        attempt.getCompletedAt(),
        choiceDtos,
        textDtos);
  }

  // ----------------- Basic reads -----------------

  public Optional<Attempt> findById(Long id) {
    return attempts.findById(id);
  }

  public Attempt getRequired(Long id) {
    return attempts.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Attempt not found: " + id));
  }

  public AttemptDto getRequiredDto(Long id) {
    Attempt attempt = getRequired(id);
    return convertToDto(attempt);
  }

  public AttemptDto getRequiredDtoWithAnswers(Long id) {
    Attempt attempt = getRequired(id);
    return convertToDtoWithAnswers(attempt);
  }

  // ----------------- Query methods -----------------

  public List<AttemptDto> findByQuizId(Long quizId) {
    return attempts.findByQuizId(quizId).stream()
        .map(this::convertToDto)
        .collect(Collectors.toList());
  }

  public List<AttemptDto> findByUserId(UUID userId) {
    return attempts.findByUserId(userId).stream()
        .map(this::convertToDto)
        .collect(Collectors.toList());
  }

  public List<AttemptDto> findByUserIdAndCategoryId(UUID userId, Long categoryId) {
    return attempts.findByUserIdAndCategoryId(userId, categoryId).stream()
        .map(this::convertToDto)
        .collect(Collectors.toList());
  }

  public Optional<AttemptDto> findMostRecentByQuizId(Long quizId) {
    return attempts.findMostRecentByQuizId(quizId)
        .map(this::convertToDto);
  }

  public List<AttemptDto> findCompletedByUserId(UUID userId) {
    return attempts.findCompletedByUserId(userId).stream()
        .map(this::convertToDto)
        .collect(Collectors.toList());
  }

  public List<AttemptDto> findIncompleteByUserId(UUID userId) {
    return attempts.findIncompleteByUserId(userId).stream()
        .map(this::convertToDto)
        .collect(Collectors.toList());
  }

  public long countByUserId(UUID userId) {
    return attempts.countByUserId(userId);
  }

  public long countCompletedByUserId(UUID userId) {
    return attempts.countCompletedByUserId(userId);
  }

  /**
   * Calculate overall success rate for a user (correct answers / total questions)
   */
  public double calculateSuccessRateByUserId(UUID userId) {
    long correctChoices = selectedChoices.countCorrectChoicesByUserId(userId);
    long totalChoices = selectedChoices.countTotalChoicesByUserId(userId);
    long correctTextAnswers = textAnswers.countCorrectTextAnswersByUserId(userId);
    long totalTextAnswers = textAnswers.countTotalTextAnswersByUserId(userId);

    long totalCorrectAnswers = correctChoices + correctTextAnswers;
    long totalQuestions = totalChoices + totalTextAnswers;

    // Debug logging
    System.out.println("DEBUG - calculateSuccessRateByUserId for userId: " + userId);
    System.out.println("  correctChoices: " + correctChoices);
    System.out.println("  totalChoices: " + totalChoices);
    System.out.println("  correctTextAnswers: " + correctTextAnswers);
    System.out.println("  totalTextAnswers: " + totalTextAnswers);
    System.out.println("  totalCorrectAnswers: " + totalCorrectAnswers);
    System.out.println("  totalQuestions: " + totalQuestions);

    return totalQuestions > 0 ? (double) totalCorrectAnswers / totalQuestions : 0.0;
  }

  /**
   * Calculate score for a specific attempt
   */
  public AttemptScoreDto calculateAttemptScore(Long attemptId) {
    Attempt attempt = getRequired(attemptId);

    long correctChoices = selectedChoices.countCorrectChoicesByAttemptId(attemptId);
    long correctTextAnswers = textAnswers.countCorrectTextAnswersByAttemptId(attemptId);

    // Count distinct questions answered, not individual choices
    long totalChoiceQuestions = selectedChoiceService.getAnsweredQuestionCount(attemptId);
    long totalTextQuestions = textAnswers.countByAttemptId(attemptId);

    long totalCorrectAnswers = correctChoices + correctTextAnswers;
    long totalQuestions = totalChoiceQuestions + totalTextQuestions;

    return new AttemptScoreDto(
        attemptId,
        attempt.getQuiz().getId(),
        totalCorrectAnswers,
        totalQuestions,
        totalQuestions > 0 ? (double) totalCorrectAnswers / totalQuestions : 0.0,
        attempt.getCompletedAt());
  }

  /**
   * Get detailed quiz results for all completed attempts by a user
   */
  public List<AttemptScoreDto> getQuizResultsByUserId(UUID userId) {
    List<Attempt> completedAttempts = attempts.findCompletedByUserId(userId);

    return completedAttempts.stream()
        .map(attempt -> calculateAttemptScore(attempt.getId()))
        .collect(Collectors.toList());
  }

  // DTO for attempt scores
  public record AttemptScoreDto(
      Long attemptId,
      Long quizId,
      Long correctAnswers,
      Long totalQuestions,
      Double successRate,
      OffsetDateTime completedAt) {
  }

  // ----------------- Creation & management -----------------

  /**
   * Start a new attempt for a quiz
   */
  @Transactional
  public AttemptDto startAttempt(Long quizId) {
    Objects.requireNonNull(quizId, "quizId must not be null");

    Quiz quiz = quizService.getRequired(quizId);

    Attempt attempt = new Attempt();
    attempt.setQuiz(quiz);
    // startedAt is set automatically by @CreationTimestamp

    Attempt savedAttempt = attempts.save(attempt);
    return convertToDto(savedAttempt);
  }

  /**
   * Submit selected choices for an attempt
   */
  @Transactional
  public AttemptDto submitChoices(Long attemptId, AttemptRequest.SubmitChoices request) {
    Objects.requireNonNull(attemptId, "attemptId must not be null");
    Objects.requireNonNull(request, "request must not be null");

    Attempt attempt = getRequired(attemptId);

    if (attempt.isCompleted()) {
      throw new IllegalStateException("Cannot submit choices for completed attempt: " + attemptId);
    }

    // Save selected choices
    for (AttemptRequest.SubmitChoices.SelectedChoiceSubmission choice : request.selectedChoices()) {
      AttemptSelectedChoice selectedChoice = new AttemptSelectedChoice();
      selectedChoice.setAttemptId(attemptId);
      selectedChoice.setQuestionId(choice.questionId());
      selectedChoice.setChoiceId(choice.choiceId());
      selectedChoice.setAttempt(attempt);
      selectedChoice.setQuiz(attempt.getQuiz());
      // selectedAt is set automatically by @CreationTimestamp

      selectedChoices.save(selectedChoice);
    }

    return convertToDto(attempt);
  }

  /**
   * Submit text answers for an attempt
   */
  @Transactional
  public AttemptDto submitTextAnswers(Long attemptId, AttemptRequest.SubmitTextAnswers request) {
    Objects.requireNonNull(attemptId, "attemptId must not be null");
    Objects.requireNonNull(request, "request must not be null");

    Attempt attempt = getRequired(attemptId);

    if (attempt.isCompleted()) {
      throw new IllegalStateException("Cannot submit text answers for completed attempt: " + attemptId);
    }

    // Save text answers
    for (AttemptRequest.SubmitTextAnswers.TextAnswerSubmission textAnswer : request.textAnswers()) {
      AttemptTextAnswer answer = new AttemptTextAnswer();
      answer.setAttemptId(attemptId);
      answer.setQuestionId(textAnswer.questionId());
      answer.setAnswerText(textAnswer.answerText());
      answer.setAttempt(attempt);
      answer.setQuiz(attempt.getQuiz());
      // answeredAt is set automatically by @CreationTimestamp

      textAnswers.save(answer);
    }

    return convertToDto(attempt);
  }

  /**
   * Complete an attempt (mark as finished)
   */
  @Transactional
  public AttemptDto completeAttempt(Long attemptId) {
    Objects.requireNonNull(attemptId, "attemptId must not be null");

    Attempt attempt = getRequired(attemptId);

    if (attempt.isCompleted()) {
      throw new IllegalStateException("Attempt already completed: " + attemptId);
    }

    // Mark the attempt as completed
    attempt.setCompletedAt(OffsetDateTime.now());
    Attempt savedAttempt = attempts.save(attempt);

    // Also mark the associated quiz as completed
    quizService.markCompleted(attempt.getQuiz().getId());

    return convertToDto(savedAttempt);
  }

  /**
   * Score a text answer (admin/teacher functionality)
   */
  @Transactional
  public AttemptDto.AttemptTextAnswerDto scoreTextAnswer(Long attemptId, AttemptRequest.ScoreTextAnswer request) {
    Objects.requireNonNull(attemptId, "attemptId must not be null");
    Objects.requireNonNull(request, "request must not be null");
    Objects.requireNonNull(request.questionId(), "questionId must not be null");

    AttemptTextAnswer answer = textAnswers.findByAttemptIdAndQuestionId(attemptId, request.questionId())
        .orElseThrow(() -> new IllegalArgumentException(
            "Text answer not found for attempt " + attemptId + " and question " + request.questionId()));

    answer.setScore(request.score());
    answer.setIsCorrect(request.isCorrect());
    answer.setFeedback(request.feedback());

    AttemptTextAnswer savedAnswer = textAnswers.save(answer);

    return AttemptDto.AttemptTextAnswerDto.from(
        savedAnswer.getAttemptId(),
        savedAnswer.getQuestionId(),
        savedAnswer.getAnswerText(),
        savedAnswer.getScore(),
        savedAnswer.getIsCorrect(),
        savedAnswer.getFeedback(),
        savedAnswer.getAnsweredAt());
  }

  // ----------------- Admin queries -----------------

  /**
   * Find all unscored text answers (for grading interface)
   */
  public List<AttemptDto.AttemptTextAnswerDto> findUnscoredTextAnswers() {
    return textAnswers.findUnscored().stream()
        .map(answer -> AttemptDto.AttemptTextAnswerDto.from(
            answer.getAttemptId(),
            answer.getQuestionId(),
            answer.getAnswerText(),
            answer.getScore(),
            answer.getIsCorrect(),
            answer.getFeedback(),
            answer.getAnsweredAt()))
        .collect(Collectors.toList());
  }

  /**
   * Find unscored text answers for a specific attempt
   */
  public List<AttemptDto.AttemptTextAnswerDto> findUnscoredTextAnswersByAttemptId(Long attemptId) {
    return textAnswers.findUnscoredByAttemptId(attemptId).stream()
        .map(answer -> AttemptDto.AttemptTextAnswerDto.from(
            answer.getAttemptId(),
            answer.getQuestionId(),
            answer.getAnswerText(),
            answer.getScore(),
            answer.getIsCorrect(),
            answer.getFeedback(),
            answer.getAnsweredAt()))
        .collect(Collectors.toList());
  }
}
