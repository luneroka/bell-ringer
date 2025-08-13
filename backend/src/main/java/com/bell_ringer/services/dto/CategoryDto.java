package com.bell_ringer.services.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * DTO for Category entity to transfer category data without exposing internal
 * entity structure.
 * Used for API responses and requests to avoid direct entity exposure.
 */
public record CategoryDto(
    Long id,

    @NotBlank @Size(max = 150) String name,

    @NotBlank @Size(max = 200) String slug,

    Long parentId,

    String parentName,

    List<CategoryDto> children,

    boolean hasChildren,

    long questionCount,

    OffsetDateTime createdAt,

    OffsetDateTime updatedAt) {

  /**
   * Creates a CategoryDto for requests when creating new categories (without ID
   * and timestamps).
   */
  public static CategoryDto forCreation(String name, Long parentId) {
    return new CategoryDto(null, name, null, parentId, null, null, false, 0, null, null);
  }

  /**
   * Creates a CategoryDto for responses (with ID and timestamps) without
   * children.
   */
  public static CategoryDto forResponse(Long id, String name, String slug,
      Long parentId, String parentName, boolean hasChildren,
      long questionCount, OffsetDateTime createdAt,
      OffsetDateTime updatedAt) {
    return new CategoryDto(id, name, slug, parentId, parentName, null, hasChildren,
        questionCount, createdAt, updatedAt);
  }

  /**
   * Creates a CategoryDto for responses with children included.
   */
  public static CategoryDto forResponseWithChildren(Long id, String name, String slug,
      Long parentId, String parentName,
      List<CategoryDto> children, long questionCount,
      OffsetDateTime createdAt, OffsetDateTime updatedAt) {
    boolean hasChildren = children != null && !children.isEmpty();
    return new CategoryDto(id, name, slug, parentId, parentName, children, hasChildren,
        questionCount, createdAt, updatedAt);
  }

  /**
   * Creates a simplified CategoryDto for basic responses (typically for child
   * categories).
   */
  public static CategoryDto forBasicResponse(Long id, String name, String slug,
      Long parentId, String parentName, long questionCount,
      OffsetDateTime createdAt, OffsetDateTime updatedAt) {
    return new CategoryDto(id, name, slug, parentId, parentName, null, false,
        questionCount, createdAt, updatedAt);
  }
}
