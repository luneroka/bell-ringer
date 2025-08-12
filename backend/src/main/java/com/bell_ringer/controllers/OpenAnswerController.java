package com.bell_ringer.controllers;

import com.bell_ringer.services.OpenAnswerService;
import com.bell_ringer.services.dto.OpenAnswerDto;
import org.springframework.http.ResponseEntity;
import org.springframework.http.CacheControl;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.constraints.Positive;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/open-answers")
@Validated
public class OpenAnswerController {

  private final OpenAnswerService openAnswerService;

  public OpenAnswerController(OpenAnswerService openAnswerService) {
    this.openAnswerService = openAnswerService;
  }

  // -------------------- Read Operations --------------------

  /** Get an open answer by ID */
  @Transactional(readOnly = true)
  @GetMapping("/{id}")
  public ResponseEntity<OpenAnswerDto> getById(@PathVariable @Positive Long id) {
    Optional<OpenAnswerDto> openAnswer = openAnswerService.getDto(id);
    return openAnswer.map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  /** Get all open answers for a specific question */
  @Transactional(readOnly = true)
  @GetMapping("/question/{questionId}")
  public ResponseEntity<List<OpenAnswerDto>> getByQuestionId(@PathVariable @Positive Long questionId) {
    List<OpenAnswerDto> openAnswers = openAnswerService.listByQuestionDto(questionId);
    return ResponseEntity.ok()
        .cacheControl(CacheControl.maxAge(Duration.ofSeconds(300)))
        .body(openAnswers);
  }

  /** Find open answer by question and answer text (case-insensitive) */
  @Transactional(readOnly = true)
  @GetMapping("/search")
  public ResponseEntity<OpenAnswerDto> findByQuestionAndAnswer(
      @RequestParam @Positive Long questionId,
      @RequestParam String answer) {
    Optional<OpenAnswerDto> openAnswer = openAnswerService.findByQuestionAndAnswerDto(questionId, answer);
    return openAnswer.map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  /** Check if an open answer exists for question and answer text */
  @Transactional(readOnly = true)
  @GetMapping("/exists")
  public ResponseEntity<Map<String, Object>> checkExists(
      @RequestParam @Positive Long questionId,
      @RequestParam String answer) {
    boolean exists = openAnswerService.existsByQuestionAndAnswer(questionId, answer);
    Map<String, Object> response = Map.of(
        "questionId", questionId,
        "answer", answer,
        "exists", exists);
    return ResponseEntity.ok()
        .cacheControl(CacheControl.maxAge(Duration.ofSeconds(60)))
        .body(response);
  }

  // -------------------- Rubric Operations --------------------

  /** Get rubric keywords for an open answer */
  @Transactional(readOnly = true)
  @GetMapping("/{id}/rubric")
  public ResponseEntity<Map<String, Object>> getRubric(@PathVariable @Positive Long id) {
    Optional<OpenAnswerDto> openAnswer = openAnswerService.getDto(id);
    if (openAnswer.isEmpty()) {
      return ResponseEntity.notFound().build();
    }

    OpenAnswerDto dto = openAnswer.get();
    Map<String, Object> rubricInfo = Map.of(
        "id", dto.id(),
        "hasRubric", dto.hasRubric(),
        "rubricKeywords", dto.rubricKeywords() != null ? dto.rubricKeywords() : Map.of(),
        "mustKeywords", dto.getMustKeywords(),
        "shouldKeywords", dto.getShouldKeywords(),
        "minScore", dto.getEffectiveMinScore());

    return ResponseEntity.ok()
        .cacheControl(CacheControl.maxAge(Duration.ofSeconds(300)))
        .body(rubricInfo);
  }

  /** Get scoring information for rubric evaluation */
  @Transactional(readOnly = true)
  @GetMapping("/{id}/scoring-info")
  public ResponseEntity<Map<String, Object>> getScoringInfo(@PathVariable @Positive Long id) {
    Optional<OpenAnswerDto> openAnswer = openAnswerService.getDto(id);
    if (openAnswer.isEmpty()) {
      return ResponseEntity.notFound().build();
    }

    OpenAnswerDto dto = openAnswer.get();
    Map<String, Object> scoringInfo = Map.of(
        "id", dto.id(),
        "answer", dto.answer(),
        "hasRubric", dto.hasRubric(),
        "minScore", dto.getEffectiveMinScore(),
        "mustKeywords", dto.getMustKeywords(),
        "shouldKeywords", dto.getShouldKeywords(),
        "mustKeywordCount", dto.getMustKeywords().size(),
        "shouldKeywordCount", dto.getShouldKeywords().size());

    return ResponseEntity.ok()
        .cacheControl(CacheControl.maxAge(Duration.ofSeconds(300)))
        .body(scoringInfo);
  }
}
