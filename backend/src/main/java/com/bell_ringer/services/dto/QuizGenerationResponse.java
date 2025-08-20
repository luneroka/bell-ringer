package com.bell_ringer.services.dto;

import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * Response DTO for quiz generation that includes questions and attempt
 * information
 */
public record QuizGenerationResponse(
    @NotNull Long quizId,
    @NotNull Long attemptId,
    @NotNull List<QuestionDto> questions) {
}
