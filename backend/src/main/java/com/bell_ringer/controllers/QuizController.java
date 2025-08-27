
package com.bell_ringer.controllers;

import com.bell_ringer.services.QuizService;
import com.bell_ringer.services.dto.QuizDto;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/quizzes")
public class QuizController {

    private final QuizService quizService;

    public QuizController(QuizService quizService) {
        this.quizService = quizService;
    }

    // -------------------- Endpoints --------------------

    /** Get a quiz by id */
    @Transactional(readOnly = true)
    @GetMapping("/{id}")
    public QuizDto getById(@PathVariable Long id) {
        return quizService.getRequiredDto(id);
    }

    /** Get a quiz by id with question IDs included */
    @Transactional(readOnly = true)
    @GetMapping("/{id}/detailed")
    public QuizDto getByIdWithQuestions(@PathVariable Long id) {
        return quizService.getRequiredDtoWithQuestions(id);
    }

    /** Mark a quiz as completed (sets completed_at = now) */
    @PostMapping("/{id}/complete")
    public QuizDto markCompleted(@PathVariable Long id) {
        return quizService.markCompletedDto(id);
    }

    /** Clear completion flag (admin/debug) */
    @DeleteMapping("/{id}/complete")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void clearCompleted(@PathVariable Long id) {
        quizService.clearCompleted(id);
    }

    /** Count completed quizzes for a user in a category */
    @GetMapping("/history/count")
    public Map<String, Object> countCompleted(@RequestParam UUID userId, @RequestParam Long categoryId) {
        long count = quizService.countCompletedByUserAndCategory(userId, categoryId);
        return Map.of("userId", userId, "categoryId", categoryId, "completedCount", count);
    }

    /** Accuracy per difficulty for a user in a category */
    @GetMapping("/accuracy")
    public Map<String, Object> accuracy(@RequestParam UUID userId, @RequestParam Long categoryId) {
        QuizService.Accuracy acc = quizService.loadAccuracy(userId, categoryId);
        return Map.of(
                "userId", userId,
                "categoryId", categoryId,
                "easy", acc.easy(),
                "medium", acc.medium(),
                "hard", acc.hard());
    }

    /** Get difficulty distribution for a quiz */
    @GetMapping("/{id}/difficulty-distribution")
    public Map<String, Object> getDifficultyDistribution(@PathVariable Long id) {
        QuizService.DifficultyDistribution distribution = quizService.getQuizDifficultyDistribution(id);
        return Map.of(
                "quizId", id,
                "easy", distribution.easy(),
                "medium", distribution.medium(),
                "hard", distribution.hard(),
                "primaryDifficulty", distribution.primaryDifficulty());
    }
}
