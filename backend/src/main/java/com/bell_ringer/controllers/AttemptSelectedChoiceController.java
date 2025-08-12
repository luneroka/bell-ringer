package com.bell_ringer.controllers;

import com.bell_ringer.models.id.AttemptSelectedChoiceId;
import com.bell_ringer.services.AttemptSelectedChoiceService;
import com.bell_ringer.services.dto.AttemptSelectedChoiceDto;
import com.bell_ringer.services.dto.AttemptSelectedChoiceRequest;

import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/v1/attempt-choices")
@Validated
public class AttemptSelectedChoiceController {

  private final AttemptSelectedChoiceService attemptSelectedChoiceService;

  public AttemptSelectedChoiceController(AttemptSelectedChoiceService attemptSelectedChoiceService) {
    this.attemptSelectedChoiceService = attemptSelectedChoiceService;
  }

  // -------------------- Basic CRUD --------------------

  /**
   * Get a specific selected choice by composite ID
   */
  @GetMapping("/{attemptId}/{questionId}/{choiceId}")
  public ResponseEntity<AttemptSelectedChoiceDto> getById(
      @PathVariable @Positive Long attemptId,
      @PathVariable @Positive Long questionId,
      @PathVariable @Positive Long choiceId) {
    AttemptSelectedChoiceId id = new AttemptSelectedChoiceId(attemptId, questionId, choiceId);
    AttemptSelectedChoiceDto selectedChoice = attemptSelectedChoiceService.getRequiredDto(id);
    return ResponseEntity.ok()
        .cacheControl(CacheControl.maxAge(30, TimeUnit.SECONDS))
        .body(selectedChoice);
  }

  // -------------------- Query endpoints --------------------

  /**
   * Get all selected choices for an attempt
   */
  @GetMapping("/attempt/{attemptId}")
  public ResponseEntity<List<AttemptSelectedChoiceDto>> getByAttemptId(@PathVariable @Positive Long attemptId) {
    List<AttemptSelectedChoiceDto> selectedChoices = attemptSelectedChoiceService.findByAttemptId(attemptId);
    return ResponseEntity.ok()
        .cacheControl(CacheControl.maxAge(30, TimeUnit.SECONDS))
        .body(selectedChoices);
  }

  /**
   * Get selected choices for a specific question in an attempt
   */
  @GetMapping("/attempt/{attemptId}/question/{questionId}")
  public ResponseEntity<List<AttemptSelectedChoiceDto>> getByAttemptIdAndQuestionId(
      @PathVariable @Positive Long attemptId,
      @PathVariable @Positive Long questionId) {
    List<AttemptSelectedChoiceDto> selectedChoices = attemptSelectedChoiceService
        .findByAttemptIdAndQuestionId(attemptId, questionId);
    return ResponseEntity.ok()
        .cacheControl(CacheControl.maxAge(30, TimeUnit.SECONDS))
        .body(selectedChoices);
  }

  /**
   * Get choice selection statistics for an attempt
   */
  @GetMapping("/attempt/{attemptId}/stats")
  public ResponseEntity<Map<String, Object>> getAttemptStats(@PathVariable @Positive Long attemptId) {
    long totalChoices = attemptSelectedChoiceService.countByAttemptId(attemptId);
    long answeredQuestions = attemptSelectedChoiceService.getAnsweredQuestionCount(attemptId);

    Map<String, Object> stats = Map.of(
        "attemptId", attemptId,
        "totalChoicesSelected", totalChoices,
        "questionsAnswered", answeredQuestions);

    return ResponseEntity.ok()
        .cacheControl(CacheControl.maxAge(60, TimeUnit.SECONDS))
        .body(stats);
  }

  // -------------------- Submit choices --------------------

  /**
   * Submit a single choice selection
   */
  @PostMapping
  public ResponseEntity<AttemptSelectedChoiceDto> submitChoice(
      @Valid @RequestBody AttemptSelectedChoiceRequest.Submit request) {
    AttemptSelectedChoiceDto selectedChoice = attemptSelectedChoiceService.submitChoice(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(selectedChoice);
  }

  /**
   * Submit multiple choice selections in batch
   */
  @PostMapping("/batch")
  public ResponseEntity<List<AttemptSelectedChoiceDto>> submitChoicesBatch(
      @Valid @RequestBody AttemptSelectedChoiceRequest.SubmitBatch request) {
    List<AttemptSelectedChoiceDto> selectedChoices = attemptSelectedChoiceService.submitChoicesBatch(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(selectedChoices);
  }

  // -------------------- Update choices --------------------

  /**
   * Update a choice selection (change from one choice to another)
   */
  @PutMapping("/{attemptId}/{questionId}/{oldChoiceId}")
  public ResponseEntity<AttemptSelectedChoiceDto> updateChoice(
      @PathVariable @Positive Long attemptId,
      @PathVariable @Positive Long questionId,
      @PathVariable @Positive Long oldChoiceId,
      @Valid @RequestBody AttemptSelectedChoiceRequest.Update request) {
    AttemptSelectedChoiceDto updatedChoice = attemptSelectedChoiceService.updateChoice(
        attemptId, questionId, oldChoiceId, request);
    return ResponseEntity.ok(updatedChoice);
  }

  // -------------------- Delete operations --------------------

  /**
   * Remove a specific choice selection
   */
  @DeleteMapping("/{attemptId}/{questionId}/{choiceId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void removeChoice(
      @PathVariable @Positive Long attemptId,
      @PathVariable @Positive Long questionId,
      @PathVariable @Positive Long choiceId) {
    attemptSelectedChoiceService.removeChoice(attemptId, questionId, choiceId);
  }

  /**
   * Remove all choice selections for an attempt
   */
  @DeleteMapping("/attempt/{attemptId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void removeAllChoicesForAttempt(@PathVariable @Positive Long attemptId) {
    attemptSelectedChoiceService.removeAllChoicesForAttempt(attemptId);
  }

  // -------------------- Analytics --------------------

  /**
   * Check if a question has been answered in an attempt
   */
  @GetMapping("/attempt/{attemptId}/question/{questionId}/answered")
  public ResponseEntity<Map<String, Object>> isQuestionAnswered(
      @PathVariable @Positive Long attemptId,
      @PathVariable @Positive Long questionId) {
    boolean answered = attemptSelectedChoiceService.hasAnsweredQuestion(attemptId, questionId);

    Map<String, Object> response = Map.of(
        "attemptId", attemptId,
        "questionId", questionId,
        "answered", answered);

    return ResponseEntity.ok()
        .cacheControl(CacheControl.maxAge(30, TimeUnit.SECONDS))
        .body(response);
  }
}
