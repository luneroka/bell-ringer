package com.bell_ringer.services.dto;

import java.util.UUID;

public record GenerationRequest(
        UUID userId,
        Long quizId,
        Long categoryId,
        int total,
        Mode modeOverride, // null means auto-decide
        String difficultyFilter // null means any difficulty, otherwise "EASY", "MEDIUM", or "HARD"
) {
    public enum Mode {
        RANDOM, ADAPTIVE
    }
}