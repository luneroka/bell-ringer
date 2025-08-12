package com.bell_ringer.controllers;

import com.bell_ringer.services.QuestionService;
import com.bell_ringer.services.QuestionService.QuotaDTO;
import com.bell_ringer.services.dto.GenerationRequest;
import com.bell_ringer.services.dto.QuestionDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/questions")
public class QuestionController {

  private final QuestionService questionService;

  public QuestionController(QuestionService questionService) {
    this.questionService = questionService;
  }

  @GetMapping("/{id}")
  public ResponseEntity<QuestionDto> getById(@PathVariable Long id) {
    QuestionDto question = questionService.getQuestionDtoById(id);
    return ResponseEntity.ok(question);
  }

  @PostMapping("/generate")
  public ResponseEntity<List<QuestionDto>> generate(@RequestBody GenerationRequest request) {
    if (request == null)
      return ResponseEntity.badRequest().build();
    // QuestionService handles the rest (mode, quota, draw)
    var list = questionService.generate(request);
    return ResponseEntity.ok(list);
  }

  // Optional sanity endpoint
  @PostMapping("/quota")
  public ResponseEntity<QuotaDTO> computeQuota(@RequestBody GenerationRequest request) {
    return ResponseEntity.ok(questionService.computeQuota(request));
  }
}