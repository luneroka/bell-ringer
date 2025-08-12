package com.bell_ringer.services;

import com.bell_ringer.models.Category;
import com.bell_ringer.repositories.CategoryRepository;
import com.bell_ringer.services.dto.CategoryDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categories;

    public CategoryService(CategoryRepository categories) {
        this.categories = categories;
    }

    // ===== DTO Conversion Methods =====

    /**
     * Convert Category entity to CategoryDto without children (for performance).
     */
    private CategoryDto convertToDto(Category category) {
        Long parentId = category.getParent() != null ? category.getParent().getId() : null;
        String parentName = category.getParent() != null ? category.getParent().getName() : null;
        boolean hasChildren = !category.getChildren().isEmpty();
        long questionCount = category.getQuestions().size();

        return CategoryDto.forResponse(
                category.getId(),
                category.getArea(),
                category.getName(),
                category.getSlug(),
                parentId,
                parentName,
                hasChildren,
                questionCount,
                category.getCreatedAt(),
                category.getUpdatedAt());
    }

    /**
     * Convert Category entity to CategoryDto with children included.
     */
    private CategoryDto convertToDtoWithChildren(Category category) {
        Long parentId = category.getParent() != null ? category.getParent().getId() : null;
        String parentName = category.getParent() != null ? category.getParent().getName() : null;
        long questionCount = category.getQuestions().size();

        List<CategoryDto> childrenDtos = category.getChildren().stream()
                .map(this::convertToDtoBasic)
                .toList();

        return CategoryDto.forResponseWithChildren(
                category.getId(),
                category.getArea(),
                category.getName(),
                category.getSlug(),
                parentId,
                parentName,
                childrenDtos,
                questionCount,
                category.getCreatedAt(),
                category.getUpdatedAt());
    }

    /**
     * Convert Category entity to basic CategoryDto (for child categories).
     */
    private CategoryDto convertToDtoBasic(Category category) {
        Long parentId = category.getParent() != null ? category.getParent().getId() : null;
        String parentName = category.getParent() != null ? category.getParent().getName() : null;
        long questionCount = category.getQuestions().size();

        return CategoryDto.forBasicResponse(
                category.getId(),
                category.getArea(),
                category.getName(),
                category.getSlug(),
                parentId,
                parentName,
                questionCount,
                category.getCreatedAt(),
                category.getUpdatedAt());
    }

    /**
     * Convert list of Category entities to list of CategoryDtos.
     */
    private List<CategoryDto> convertToDtoList(List<Category> categories) {
        return categories.stream()
                .map(this::convertToDto)
                .toList();
    }

    /** Get a category by id or throw a clear error. */
    public Category getRequired(Long id) {
        if (id == null)
            throw new IllegalArgumentException("categoryId must not be null");
        return categories.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Category not found: " + id));
    }

    /** Get a category by id as DTO or throw a clear error. */
    public CategoryDto getRequiredDto(Long id) {
        Category category = getRequired(id);
        return convertToDto(category);
    }

    /** List all root categories (no parent). */
    public List<Category> listRoots() {
        return categories.findAllByParentIsNull();
    }

    /** List all root categories as DTOs (no parent). */
    public List<CategoryDto> listRootsDto() {
        List<Category> rootCategories = categories.findAllByParentIsNull();
        return convertToDtoList(rootCategories);
    }

    /** List direct children of a parent category. */
    public List<Category> listChildren(Long parentId) {
        if (parentId == null)
            return Collections.emptyList();
        return categories.findAllByParentId(parentId);
    }

    /** List direct children of a parent category as DTOs. */
    public List<CategoryDto> listChildrenDto(Long parentId) {
        if (parentId == null)
            return Collections.emptyList();
        List<Category> childCategories = categories.findAllByParentId(parentId);
        return convertToDtoList(childCategories);
    }

    /**
     * Resolve the effective category ids to use when a user selects a category.
     * If the selected category has children, return [parent + children] ids.
     * If it's a leaf, return just [id].
     */
    public List<Long> resolveSelectionIds(Long categoryId) {
        if (categoryId == null)
            throw new IllegalArgumentException("categoryId must not be null");
        List<Integer> ids = categories.getParentAndChildrenIds(categoryId);
        if (ids == null || ids.isEmpty()) {
            return List.of(categoryId);
        }
        return ids.stream()
                .map(Integer::longValue)
                .collect(java.util.stream.Collectors.toList());
    }

    // ---------- Write methods ---------- //

    /** Create a new category. Parent is optional (null for root). */
    @Transactional
    public Category create(String area, String name, Long parentId) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        Category parent = null;
        if (parentId != null) {
            parent = getRequired(parentId);
            // Guard against duplicates under the same parent (case-insensitive)
            if (categories.existsByParentIdAndNameIgnoreCase(parentId, name)) {
                throw new IllegalArgumentException("Category already exists under parent: " + name);
            }
        } else {
            if (categories.existsByParentIdAndNameIgnoreCase(null, name)) {
                throw new IllegalArgumentException("Root category already exists: " + name);
            }
        }

        Category c = new Category();
        c.setArea(area);
        c.setName(name.trim());
        c.setParent(parent);
        // slug will be generated by entity lifecycle if blank
        c.setSlug(null);
        return categories.save(c);
    }

    /** Create a new category and return as DTO. */
    @Transactional
    public CategoryDto createDto(String area, String name, Long parentId) {
        Category category = create(area, name, parentId);
        return convertToDto(category);
    }

    /** Rename and/or move a category under a new parent. */
    @Transactional
    public Category update(Long id, String newArea, String newName, Long newParentId) {
        Category c = getRequired(id);
        String name = Optional.ofNullable(newName).map(String::trim).orElse(c.getName());

        Category parent = c.getParent();
        if (!Objects.equals(newParentId, parent == null ? null : parent.getId())) {
            parent = (newParentId == null) ? null : getRequired(newParentId);
        }

        // Guard duplicates under the target parent (case-insensitive)
        Long parentIdForCheck = (parent == null) ? null : parent.getId();
        Optional<Category> duplicate = categories.findByParentIdAndNameIgnoreCase(parentIdForCheck, name);
        if (duplicate.isPresent() && !duplicate.get().getId().equals(id)) {
            throw new IllegalArgumentException("Another category with the same name exists under this parent: " + name);
        }

        c.setArea(newArea != null ? newArea : c.getArea());
        c.setName(name);
        c.setParent(parent);
        // Clear slug so entity rebuilds it on update
        c.setSlug(null);
        return categories.save(c);
    }

    /**
     * Delete a category. Caller should ensure it is safe (no dependent data) or
     * rely on FK rules.
     */
    @Transactional
    public void delete(Long id) {
        if (id == null)
            return;
        categories.deleteById(id);
    }
}
