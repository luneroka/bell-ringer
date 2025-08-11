package com.bell_ringer.services.dto;

import java.util.UUID;

public record GenerationRequest(
    UUID userId,
    Long quizId,
    Long categoryId,
    int total,
    Mode modeOverride // null means auto-decide
) {
    public enum Mode { RANDOM, ADAPTIVE }
}