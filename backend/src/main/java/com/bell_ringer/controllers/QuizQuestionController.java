
package com.bell_ringer.controllers;

import com.bell_ringer.services.QuizQuestionService;
import com.bell_ringer.services.dto.QuizQuestionRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/quizzes/{quizId}/questions")
public class QuizQuestionController {

    private final QuizQuestionService quizQuestionService;

    public QuizQuestionController(QuizQuestionService quizQuestionService) {
        this.quizQuestionService = quizQuestionService;
    }

    // ---------------- Reads ----------------

    /** Return only question IDs linked to this quiz. */
    @GetMapping("/ids")
    public Map<String, Object> listQuestionIds(@PathVariable Long quizId) {
        List<Long> ids = quizQuestionService.findQuestionIdsByQuizId(quizId);
        long count = ids.size();
        return Map.of("quizId", quizId, "questionIds", ids, "count", count);
    }

    /** Count how many questions are attached to a quiz. */
    @GetMapping("/count")
    public Map<String, Object> count(@PathVariable Long quizId) {
        long count = quizQuestionService.countByQuizId(quizId);
        return Map.of("quizId", quizId, "count", count);
    }

    // ---------------- Writes ----------------

    /** Add a single question to a quiz. */
    @PostMapping("/{questionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void addOne(@PathVariable Long quizId, @PathVariable Long questionId) {
        quizQuestionService.addIfAbsent(quizId, questionId);
    }

    /** Add many questions to a quiz (duplicates ignored). */
    @PostMapping
    public ResponseEntity<?> addMany(@PathVariable Long quizId, @Valid @RequestBody QuizQuestionRequest.AddMany body) {
        var created = quizQuestionService.addAllIfAbsent(quizId, body.questionIds());
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "quizId", quizId,
                "created", created.size()));
    }

    /** Replace all links with the provided ordered list. */
    @PutMapping
    public Map<String, Object> replaceAll(@PathVariable Long quizId, @RequestBody QuizQuestionRequest.ReplaceAll body) {
        List<Long> finalOrder = quizQuestionService.replaceAll(quizId, body == null ? null : body.questionIds());
        return Map.of("quizId", quizId, "questionIds", finalOrder, "count", finalOrder.size());
    }

    /** Remove a single question from a quiz. */
    @DeleteMapping("/{questionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeOne(@PathVariable Long quizId, @PathVariable Long questionId) {
        quizQuestionService.remove(quizId, questionId);
    }

    /** Clear all questions from a quiz. */
    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void clear(@PathVariable Long quizId) {
        quizQuestionService.clear(quizId);
    }
}
