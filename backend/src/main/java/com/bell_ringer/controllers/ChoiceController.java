package com.bell_ringer.controllers;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.http.CacheControl;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.constraints.Positive;
import java.time.Duration;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bell_ringer.services.ChoiceService;
import com.bell_ringer.services.dto.ChoiceDto;

@RestController
@RequestMapping("/api/v1/choices")
@Validated
public class ChoiceController {

  private final ChoiceService choiceService;

  public ChoiceController(ChoiceService choiceService) {
    this.choiceService = choiceService;
  }

  // GET a choice by its ID
  @GetMapping("/{id}")
  public ResponseEntity<ChoiceDto> getById(@PathVariable @Positive Long id) {
    ChoiceDto choice = choiceService.getById(id);
    return ResponseEntity.ok(choice);
  }

  // GET Choices By Question Id
  @GetMapping("/question/{questionId}")
  public ResponseEntity<List<ChoiceDto>> getChoicesByQuestionId(@PathVariable @Positive Long questionId) {
    List<ChoiceDto> choices = choiceService.getByQuestionId(questionId);
    return ResponseEntity.ok()
        .cacheControl(CacheControl.maxAge(Duration.ofSeconds(300)))
        .body(choices);
  }

  // GET is correct choice by ID
  @GetMapping("/{id}/is-correct")
  public ResponseEntity<Boolean> isCorrect(@PathVariable @Positive Long id) {
    boolean correct = choiceService.isCorrect(id);
    return ResponseEntity.ok(correct);
  }

  // GET correct choices by question ID
  @GetMapping("/question/{questionId}/correct")
  public ResponseEntity<List<ChoiceDto>> getCorrectChoicesByQuestionId(@PathVariable @Positive Long questionId) {
    List<ChoiceDto> choices = choiceService.getCorrectByQuestionId(questionId);
    return ResponseEntity.ok()
        .cacheControl(CacheControl.maxAge(Duration.ofSeconds(300)))
        .body(choices);
  }

  // GET correct choice IDs
  @GetMapping("/question/{questionId}/correct-ids")
  public ResponseEntity<List<Long>> getCorrectChoiceIdsByQuestionId(@PathVariable @Positive Long questionId) {
    List<Long> ids = choiceService.getCorrectChoiceIds(questionId);
    return ResponseEntity.ok()
        .cacheControl(CacheControl.maxAge(Duration.ofSeconds(300)))
        .body(ids);
  }
}
