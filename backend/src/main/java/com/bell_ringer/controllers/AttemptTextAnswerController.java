package com.bell_ringer.controllers;

import com.bell_ringer.models.id.AttemptTextAnswerId;
import com.bell_ringer.services.AttemptTextAnswerService;
import com.bell_ringer.services.dto.AttemptTextAnswerDto;
import com.bell_ringer.services.dto.AttemptTextAnswerRequest;

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
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/v1/attempt-text-answers")
@Validated
public class AttemptTextAnswerController {

  private final AttemptTextAnswerService attemptTextAnswerService;

  public AttemptTextAnswerController(AttemptTextAnswerService attemptTextAnswerService) {
    this.attemptTextAnswerService = attemptTextAnswerService;
  }

  // -------------------- Basic CRUD --------------------

  /**
   * Get a specific text answer by composite ID
   */
  @GetMapping("/{attemptId}/{questionId}")
  public ResponseEntity<AttemptTextAnswerDto> getById(
      @PathVariable @Positive Long attemptId,
      @PathVariable @Positive Long questionId) {
    AttemptTextAnswerId id = new AttemptTextAnswerId(attemptId, questionId);
    AttemptTextAnswerDto textAnswer = attemptTextAnswerService.getRequiredDto(id);
    return ResponseEntity.ok()
        .cacheControl(CacheControl.maxAge(30, TimeUnit.SECONDS))
        .body(textAnswer);
  }

  /**
   * Get a text answer for a specific attempt and question (alternative endpoint)
   */
  @GetMapping("/attempt/{attemptId}/question/{questionId}")
  public ResponseEntity<AttemptTextAnswerDto> getByAttemptAndQuestion(
      @PathVariable @Positive Long attemptId,
      @PathVariable @Positive Long questionId) {
    Optional<AttemptTextAnswerDto> textAnswer = attemptTextAnswerService.findByAttemptIdAndQuestionId(attemptId,
        questionId);
    return textAnswer
        .map(dto -> ResponseEntity.ok()
            .cacheControl(CacheControl.maxAge(30, TimeUnit.SECONDS))
            .body(dto))
        .orElse(ResponseEntity.notFound().build());
  }

  // -------------------- Query endpoints --------------------

  /**
   * Get all text answers for an attempt
   */
  @GetMapping("/attempt/{attemptId}")
  public ResponseEntity<List<AttemptTextAnswerDto>> getByAttemptId(@PathVariable @Positive Long attemptId) {
    List<AttemptTextAnswerDto> textAnswers = attemptTextAnswerService.findByAttemptId(attemptId);
    return ResponseEntity.ok()
        .cacheControl(CacheControl.maxAge(30, TimeUnit.SECONDS))
        .body(textAnswers);
  }

  /**
   * Get text answer statistics for an attempt
   */
  @GetMapping("/attempt/{attemptId}/stats")
  public ResponseEntity<Map<String, Object>> getAttemptStats(@PathVariable @Positive Long attemptId) {
    long totalAnswers = attemptTextAnswerService.countByAttemptId(attemptId);
    long gradedAnswers = attemptTextAnswerService.getGradedTextAnswerCount(attemptId);
    boolean allGraded = attemptTextAnswerService.areAllTextAnswersGraded(attemptId);

    Map<String, Object> stats = Map.of(
        "attemptId", attemptId,
        "totalTextAnswers", totalAnswers,
        "gradedAnswers", gradedAnswers,
        "ungradedAnswers", totalAnswers - gradedAnswers,
        "allGraded", allGraded,
        "gradingProgress", totalAnswers > 0 ? (double) gradedAnswers / totalAnswers : 0.0);

    return ResponseEntity.ok()
        .cacheControl(CacheControl.maxAge(60, TimeUnit.SECONDS))
        .body(stats);
  }

  // -------------------- Submit text answers --------------------

  /**
   * Submit a single text answer
   */
  @PostMapping
  public ResponseEntity<AttemptTextAnswerDto> submitTextAnswer(
      @Valid @RequestBody AttemptTextAnswerRequest.Submit request) {
    AttemptTextAnswerDto textAnswer = attemptTextAnswerService.submitTextAnswer(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(textAnswer);
  }

  // -------------------- Update text answers --------------------

  /**
   * Update a text answer (before grading)
   */
  @PutMapping("/{attemptId}/{questionId}")
  public ResponseEntity<AttemptTextAnswerDto> updateTextAnswer(
      @PathVariable @Positive Long attemptId,
      @PathVariable @Positive Long questionId,
      @Valid @RequestBody AttemptTextAnswerRequest.Update request) {
    AttemptTextAnswerDto updatedAnswer = attemptTextAnswerService.updateTextAnswer(attemptId, questionId, request);
    return ResponseEntity.ok(updatedAnswer);
  }

  // -------------------- Grading (Admin/Teacher) --------------------

  /**
   * Grade a text answer (admin/teacher functionality)
   */
  @PutMapping("/{attemptId}/{questionId}/grade")
  public ResponseEntity<AttemptTextAnswerDto> gradeTextAnswer(
      @PathVariable @Positive Long attemptId,
      @PathVariable @Positive Long questionId,
      @Valid @RequestBody AttemptTextAnswerRequest.Grade request) {
    AttemptTextAnswerDto gradedAnswer = attemptTextAnswerService.gradeTextAnswer(attemptId, questionId, request);
    return ResponseEntity.ok(gradedAnswer);
  }

  /**
   * Get all unscored text answers (for grading interface)
   */
  @GetMapping("/unscored")
  public ResponseEntity<List<AttemptTextAnswerDto>> getUnscoredTextAnswers() {
    List<AttemptTextAnswerDto> unscoredAnswers = attemptTextAnswerService.findUnscored();
    return ResponseEntity.ok()
        .cacheControl(CacheControl.noCache())
        .body(unscoredAnswers);
  }

  /**
   * Get unscored text answers for a specific attempt
   */
  @GetMapping("/attempt/{attemptId}/unscored")
  public ResponseEntity<List<AttemptTextAnswerDto>> getUnscoredTextAnswersByAttemptId(
      @PathVariable @Positive Long attemptId) {
    List<AttemptTextAnswerDto> unscoredAnswers = attemptTextAnswerService.findUnscoredByAttemptId(attemptId);
    return ResponseEntity.ok()
        .cacheControl(CacheControl.noCache())
        .body(unscoredAnswers);
  }

  // -------------------- Delete operations --------------------

  /**
   * Remove a specific text answer
   */
  @DeleteMapping("/{attemptId}/{questionId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void removeTextAnswer(
      @PathVariable @Positive Long attemptId,
      @PathVariable @Positive Long questionId) {
    attemptTextAnswerService.removeTextAnswer(attemptId, questionId);
  }

  /**
   * Remove all text answers for an attempt
   */
  @DeleteMapping("/attempt/{attemptId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void removeAllTextAnswersForAttempt(@PathVariable @Positive Long attemptId) {
    attemptTextAnswerService.removeAllTextAnswersForAttempt(attemptId);
  }

  // -------------------- Analytics --------------------

  /**
   * Check if a text question has been answered in an attempt
   */
  @GetMapping("/attempt/{attemptId}/question/{questionId}/answered")
  public ResponseEntity<Map<String, Object>> isQuestionAnswered(
      @PathVariable @Positive Long attemptId,
      @PathVariable @Positive Long questionId) {
    boolean answered = attemptTextAnswerService.hasAnsweredQuestion(attemptId, questionId);

    Map<String, Object> response = Map.of(
        "attemptId", attemptId,
        "questionId", questionId,
        "answered", answered);

    return ResponseEntity.ok()
        .cacheControl(CacheControl.maxAge(30, TimeUnit.SECONDS))
        .body(response);
  }
}
