package com.bell_ringer.services.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import com.bell_ringer.models.OpenAnswer;

import java.util.List;
import java.util.Map;

/**
 * DTO for OpenAnswer entity to transfer open answer data without exposing
 * internal
 * entity structure.
 * Used for API responses and requests to avoid direct entity exposure.
 */
public record OpenAnswerDto(
    Long id,

    @NotNull(message = "Question ID is required") Long questionId,

    @NotBlank(message = "Answer text is required") String answer,

    Map<String, Object> rubricKeywords,

    @Min(value = 0, message = "Minimum score must be between 0 and 100") @Max(value = 100, message = "Minimum score must be between 0 and 100") Integer minScore) {

  // Factory methods for different use cases

  /**
   * Create DTO for API responses from entity
   */
  public static OpenAnswerDto forResponse(OpenAnswer openAnswer) {
    return new OpenAnswerDto(
        openAnswer.getId(),
        openAnswer.getQuestion().getId(),
        openAnswer.getAnswer(),
        openAnswer.getRubricKeywords(),
        openAnswer.getMinScore());
  }

  /**
   * Create DTO for creation requests (without ID)
   */
  public static OpenAnswerDto forCreation(Long questionId, String answer, Map<String, Object> rubricKeywords,
      Integer minScore) {
    return new OpenAnswerDto(
        null,
        questionId,
        answer,
        rubricKeywords,
        minScore != null ? minScore : 60);
  }

  /**
   * Create DTO for creation requests (minimal - without rubric)
   */
  public static OpenAnswerDto forCreation(Long questionId, String answer) {
    return new OpenAnswerDto(
        null,
        questionId,
        answer,
        null,
        60);
  }

  /**
   * Create DTO for updates (with ID)
   */
  public static OpenAnswerDto forUpdate(Long id, String answer, Map<String, Object> rubricKeywords, Integer minScore) {
    return new OpenAnswerDto(
        id,
        null, // questionId not needed for updates
        answer,
        rubricKeywords,
        minScore);
  }

  // Helper methods for rubric keywords access

  /**
   * Extract "must" keywords from rubric
   */
  @SuppressWarnings("unchecked")
  public List<String> getMustKeywords() {
    if (rubricKeywords == null)
      return List.of();
    Object must = rubricKeywords.get("must");
    return must instanceof List ? (List<String>) must : List.of();
  }

  /**
   * Extract "should" keywords from rubric
   */
  @SuppressWarnings("unchecked")
  public List<String> getShouldKeywords() {
    if (rubricKeywords == null)
      return List.of();
    Object should = rubricKeywords.get("should");
    return should instanceof List ? (List<String>) should : List.of();
  }

  /**
   * Check if this answer has rubric keywords defined
   */
  public boolean hasRubric() {
    return rubricKeywords != null && !rubricKeywords.isEmpty();
  }

  /**
   * Get effective minimum score (defaults to 60 if null)
   */
  public int getEffectiveMinScore() {
    return minScore != null ? minScore : 60;
  }
}
