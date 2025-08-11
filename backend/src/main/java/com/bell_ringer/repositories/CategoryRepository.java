
package com.bell_ringer.repositories;

import com.bell_ringer.models.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    // Find by slug (unique)
    Optional<Category> findBySlug(String slug);

    // All root categories (no parent)
    List<Category> findAllByParentIsNull();

    // All direct children of a given parent
    List<Category> findAllByParentId(Long parentId);

    // Bulk lookup by ids
    List<Category> findAllByIdIn(Collection<Long> ids);

    // Guard against duplicates under the same parent (case-insensitive)
    boolean existsByParentIdAndNameIgnoreCase(Long parentId, String name);
    Optional<Category> findByParentIdAndNameIgnoreCase(Long parentId, String name);

    // Helper: parent id + its direct children ids (for "All Frontend" selection)
    @Query(value = """
        SELECT id
          FROM categories
         WHERE id = :parentId OR parent_id = :parentId
        ORDER BY id
    """, nativeQuery = true)
    List<Integer> getParentAndChildrenIds(@Param("parentId") Long parentId);
}
