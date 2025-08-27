package com.bell_ringer.controllers;

import com.bell_ringer.services.AttemptService;
import com.bell_ringer.services.dto.AttemptDto;
import com.bell_ringer.services.dto.AttemptRequest;

import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/v1/attempts")
@Validated
public class AttemptController {

  private final AttemptService attemptService;

  public AttemptController(AttemptService attemptService) {
    this.attemptService = attemptService;
  }

  // -------------------- Basic CRUD --------------------

  /**
   * Get an attempt by ID
   */
  @GetMapping("/{id}")
  public ResponseEntity<AttemptDto> getById(@PathVariable @Positive Long id) {
    AttemptDto attempt = attemptService.getRequiredDto(id);
    return ResponseEntity.ok()
        .cacheControl(CacheControl.maxAge(30, TimeUnit.SECONDS))
        .body(attempt);
  }

  /**
   * Get an attempt by ID with all answer details
   */
  @GetMapping("/{id}/detailed")
  public ResponseEntity<AttemptDto> getByIdDetailed(@PathVariable @Positive Long id) {
    AttemptDto attempt = attemptService.getRequiredDtoWithAnswers(id);
    return ResponseEntity.ok()
        .cacheControl(CacheControl.maxAge(10, TimeUnit.SECONDS))
        .body(attempt);
  }

  // -------------------- Query endpoints --------------------

  /**
   * Get all attempts for a quiz
   */
  @GetMapping
  public ResponseEntity<List<AttemptDto>> getByQuizId(@RequestParam @Positive Long quizId) {
    List<AttemptDto> attempts = attemptService.findByQuizId(quizId);
    return ResponseEntity.ok()
        .cacheControl(CacheControl.maxAge(60, TimeUnit.SECONDS))
        .body(attempts);
  }

  /**
   * Get all attempts for a user
   */
  @GetMapping("/user/{userId}")
  public ResponseEntity<List<AttemptDto>> getByUserId(@PathVariable UUID userId) {
    List<AttemptDto> attempts = attemptService.findByUserId(userId);
    return ResponseEntity.ok()
        .cacheControl(CacheControl.maxAge(30, TimeUnit.SECONDS))
        .body(attempts);
  }

  /**
   * Get all attempts for a user in a specific category
   */
  @GetMapping("/user/{userId}/category/{categoryId}")
  public ResponseEntity<List<AttemptDto>> getByUserIdAndCategoryId(
      @PathVariable UUID userId,
      @PathVariable @Positive Long categoryId) {
    List<AttemptDto> attempts = attemptService.findByUserIdAndCategoryId(userId, categoryId);
    return ResponseEntity.ok()
        .cacheControl(CacheControl.maxAge(30, TimeUnit.SECONDS))
        .body(attempts);
  }

  /**
   * Get the most recent attempt for a quiz
   */
  @GetMapping("/quiz/{quizId}/latest")
  public ResponseEntity<AttemptDto> getMostRecentByQuizId(@PathVariable @Positive Long quizId) {
    Optional<AttemptDto> attempt = attemptService.findMostRecentByQuizId(quizId);
    return attempt
        .map(dto -> ResponseEntity.ok()
            .cacheControl(CacheControl.maxAge(30, TimeUnit.SECONDS))
            .body(dto))
        .orElse(ResponseEntity.notFound().build());
  }

  /**
   * Get completed attempts for a user
   */
  @GetMapping("/user/{userId}/completed")
  public ResponseEntity<List<AttemptDto>> getCompletedByUserId(@PathVariable UUID userId) {
    List<AttemptDto> attempts = attemptService.findCompletedByUserId(userId);
    return ResponseEntity.ok()
        .cacheControl(CacheControl.maxAge(60, TimeUnit.SECONDS))
        .body(attempts);
  }

  /**
   * Get incomplete attempts for a user
   */
  @GetMapping("/user/{userId}/incomplete")
  public ResponseEntity<List<AttemptDto>> getIncompleteByUserId(@PathVariable UUID userId) {
    List<AttemptDto> attempts = attemptService.findIncompleteByUserId(userId);
    return ResponseEntity.ok()
        .cacheControl(CacheControl.noCache())
        .body(attempts);
  }

  // -------------------- Statistics --------------------

  /**
   * Get attempt statistics for a user
   */
  @GetMapping("/user/{userId}/stats")
  public ResponseEntity<Map<String, Object>> getUserStats(@PathVariable UUID userId) {
    long totalAttempts = attemptService.countByUserId(userId);
    long completedAttempts = attemptService.countCompletedByUserId(userId);
    long incompleteAttempts = totalAttempts - completedAttempts;
    double completionRate = totalAttempts > 0 ? (double) completedAttempts / totalAttempts : 0.0;
    double successRate = attemptService.calculateSuccessRateByUserId(userId);

    Map<String, Object> stats = Map.of(
        "userId", userId,
        "totalAttempts", totalAttempts,
        "completedAttempts", completedAttempts,
        "incompleteAttempts", incompleteAttempts,
        "completionRate", completionRate,
        "successRate", successRate);

    return ResponseEntity.ok()
        .cacheControl(CacheControl.maxAge(120, TimeUnit.SECONDS))
        .body(stats);
  }

  /**
   * Get detailed quiz results for a user (for history table)
   */
  @GetMapping("/user/{userId}/results")
  public ResponseEntity<List<AttemptService.AttemptScoreDto>> getUserQuizResults(@PathVariable UUID userId) {
    List<AttemptService.AttemptScoreDto> results = attemptService.getQuizResultsByUserId(userId);

    return ResponseEntity.ok()
        .cacheControl(CacheControl.maxAge(120, TimeUnit.SECONDS))
        .body(results);
  }

  // -------------------- Attempt management --------------------

  /**
   * Retry a quiz (start a new attempt for an existing quiz)
   * Note: Initial attempts are created automatically via /questions/generate
   */
  @PostMapping("/retry")
  public ResponseEntity<AttemptDto> retryQuiz(@Valid @RequestBody AttemptRequest.Create request) {
    AttemptDto attempt = attemptService.startAttempt(request.quizId());
    return ResponseEntity.status(HttpStatus.CREATED).body(attempt);
  }

  /**
   * Submit selected choices for an attempt
   */
  @PostMapping("/{id}/choices")
  public ResponseEntity<AttemptDto> submitChoices(
      @PathVariable @Positive Long id,
      @Valid @RequestBody AttemptRequest.SubmitChoices request) {
    AttemptDto attempt = attemptService.submitChoices(id, request);
    return ResponseEntity.ok(attempt);
  }

  /**
   * Submit text answers for an attempt
   */
  @PostMapping("/{id}/text-answers")
  public ResponseEntity<AttemptDto> submitTextAnswers(
      @PathVariable @Positive Long id,
      @Valid @RequestBody AttemptRequest.SubmitTextAnswers request) {
    AttemptDto attempt = attemptService.submitTextAnswers(id, request);
    return ResponseEntity.ok(attempt);
  }

  /**
   * Complete an attempt (mark as finished)
   */
  @PostMapping("/{id}/complete")
  public ResponseEntity<AttemptDto> completeAttempt(@PathVariable @Positive Long id) {
    AttemptDto attempt = attemptService.completeAttempt(id);
    return ResponseEntity.ok(attempt);
  }

  // -------------------- Grading (Admin/Teacher) --------------------

  /**
   * Score a text answer (admin/teacher functionality)
   */
  @PutMapping("/{id}/text-answers/score")
  public ResponseEntity<AttemptDto.AttemptTextAnswerDto> scoreTextAnswer(
      @PathVariable @Positive Long id,
      @Valid @RequestBody AttemptRequest.ScoreTextAnswer request) {
    AttemptDto.AttemptTextAnswerDto scoredAnswer = attemptService.scoreTextAnswer(id, request);
    return ResponseEntity.ok(scoredAnswer);
  }

  /**
   * Get all unscored text answers (for grading interface)
   */
  @GetMapping("/text-answers/unscored")
  public ResponseEntity<List<AttemptDto.AttemptTextAnswerDto>> getUnscoredTextAnswers() {
    List<AttemptDto.AttemptTextAnswerDto> unscoredAnswers = attemptService.findUnscoredTextAnswers();
    return ResponseEntity.ok()
        .cacheControl(CacheControl.noCache())
        .body(unscoredAnswers);
  }

  /**
   * Get unscored text answers for a specific attempt
   */
  @GetMapping("/{id}/text-answers/unscored")
  public ResponseEntity<List<AttemptDto.AttemptTextAnswerDto>> getUnscoredTextAnswersByAttemptId(
      @PathVariable @Positive Long id) {
    List<AttemptDto.AttemptTextAnswerDto> unscoredAnswers = attemptService.findUnscoredTextAnswersByAttemptId(id);
    return ResponseEntity.ok()
        .cacheControl(CacheControl.noCache())
        .body(unscoredAnswers);
  }
}
