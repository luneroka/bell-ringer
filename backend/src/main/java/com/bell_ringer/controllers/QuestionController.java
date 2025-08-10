package com.bell_ringer.controllers;

import com.bell_ringer.models.Question;
import com.bell_ringer.services.QuestionService;
import com.bell_ringer.services.dto.GenerationRequest;
import com.bell_ringer.services.QuestionService.QuotaDTO;
import org.springframework.http.HttpStatus;
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

  @PostMapping("/generate")
  public ResponseEntity<List<Question>> generate(@RequestBody GenerationRequest request) {
    if (request == null) return ResponseEntity.badRequest().build();
    // QuestionService handles the rest (mode, quota, draw)
    var list = questionService.generate(request);
    return ResponseEntity.status(HttpStatus.OK).body(list);
  }

  // Optional sanity endpoint
  @PostMapping("/quota")
  public ResponseEntity<QuotaDTO> computeQuota(@RequestBody GenerationRequest request) {
    return ResponseEntity.ok(questionService.computeQuota(request));
  }
}