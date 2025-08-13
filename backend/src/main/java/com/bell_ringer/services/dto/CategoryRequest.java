package com.bell_ringer.services.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTOs for Category operations
 */
public class CategoryRequest {

  /**
   * Request DTO for creating a new category
   */
  public record Create(
      @NotBlank(message = "Category name is required") String name,
      Long parentId // optional (null for root)
  ) {
  }

  /**
   * Request DTO for updating an existing category
   */
  public record Update(
      String name, // optional
      Long parentId // optional (null to move to root)
  ) {
  }
}
