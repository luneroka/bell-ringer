
package com.bell_ringer.controllers;

import com.bell_ringer.models.Quiz;
import com.bell_ringer.services.QuizService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/quizzes")
public class QuizController {

    private final QuizService quizService;

    public QuizController(QuizService quizService) {
        this.quizService = quizService;
    }

    // -------------------- DTOs --------------------

    public static class CreateQuizRequest {
        public UUID userId;
        public Long categoryId;
    }

    public static class CreateWithQuestionsRequest {
        public UUID userId;
        public Long categoryId;
        public List<Long> questionIds;
    }

    // -------------------- Endpoints --------------------

    /** Get a quiz by id */
    @Transactional(readOnly = true)
    @GetMapping("/{id}")
    public Quiz getById(@PathVariable Long id) {
        return quizService.getRequired(id);
    }

    /** Create an empty quiz (no questions yet) */
    @PostMapping
    public ResponseEntity<Quiz> create(@RequestBody CreateQuizRequest body) {
        if (body == null || body.userId == null || body.categoryId == null) {
            return ResponseEntity.badRequest().build();
        }
        Quiz quiz = quizService.create(body.userId, body.categoryId);
        return ResponseEntity.status(HttpStatus.CREATED).body(quiz);
    }

    /** Create a quiz and attach the provided questions */
    @PostMapping("/with-questions")
    public ResponseEntity<Quiz> createWithQuestions(@RequestBody CreateWithQuestionsRequest body) {
        if (body == null || body.userId == null || body.categoryId == null || body.questionIds == null || body.questionIds.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        Quiz quiz = quizService.createWithQuestions(body.userId, body.categoryId, body.questionIds);
        return ResponseEntity.status(HttpStatus.CREATED).body(quiz);
    }

    /** Attach additional questions to an existing quiz */
    @PostMapping("/{id}/questions")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void addQuestions(@PathVariable Long id, @RequestBody List<Long> questionIds) {
        quizService.addQuestions(id, questionIds);
    }

    /** Mark a quiz as completed (sets completed_at = now) */
    @PostMapping("/{id}/complete")
    public Quiz markCompleted(@PathVariable Long id) {
        return quizService.markCompleted(id);
    }

    /** Clear completion flag (admin/debug) */
    @DeleteMapping("/{id}/complete")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void clearCompleted(@PathVariable Long id) {
        quizService.clearCompleted(id);
    }

    /** Step 2.B: Count completed quizzes for a user in a category */
    @GetMapping("/history/count")
    public Map<String, Object> countCompleted(@RequestParam UUID userId, @RequestParam Long categoryId) {
        long count = quizService.countCompletedByUserAndCategory(userId, categoryId);
        return Map.of("userId", userId, "categoryId", categoryId, "completedCount", count);
    }

    /** Step 2.C: Accuracy per difficulty for a user in a category */
    @GetMapping("/accuracy")
    public Map<String, Object> accuracy(@RequestParam UUID userId, @RequestParam Long categoryId) {
        QuizService.Accuracy acc = quizService.loadAccuracy(userId, categoryId);
        return Map.of(
            "userId", userId,
            "categoryId", categoryId,
            "easy", acc.easy(),
            "medium", acc.medium(),
            "hard", acc.hard()
        );
    }
}
