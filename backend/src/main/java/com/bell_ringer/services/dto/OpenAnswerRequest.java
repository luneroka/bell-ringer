package com.bell_ringer.services.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

import java.util.Map;

/**
 * DTOs for OpenAnswer operations
 */
public class OpenAnswerRequest {

  /**
   * Request DTO for creating a new open answer
   */
  public record Create(
      @NotNull(message = "Question ID is required") Long questionId,

      @NotBlank(message = "Answer text is required") String answer,

      Map<String, Object> rubricKeywords,

      @Min(value = 0, message = "Minimum score must be between 0 and 100") @Max(value = 100, message = "Minimum score must be between 0 and 100") Integer minScore) {
  }

  /**
   * Request DTO for updating an existing open answer
   */
  public record Update(
      @NotBlank(message = "Answer text is required") String answer,

      Map<String, Object> rubricKeywords,

      @Min(value = 0, message = "Minimum score must be between 0 and 100") @Max(value = 100, message = "Minimum score must be between 0 and 100") Integer minScore) {
  }
}
