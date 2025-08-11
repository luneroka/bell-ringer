package com.bell_ringer.controllers;

import com.bell_ringer.models.Category;
import com.bell_ringer.services.CategoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.io.Reader;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    // ---------------- DTOs ----------------
    public static class CreateCategoryRequest {
        public String area;
        public String name;
        public Long parentId; // optional (null for root)
    }

    public static class UpdateCategoryRequest {
        public String area;     // optional
        public String name;     // optional
        public Long parentId;   // optional (null to move to root)
    }

    // ---------------- Reads ----------------

    /** Get a category by id */
    @Transactional(readOnly = true)
    @GetMapping("/{id}")
    public Category getById(@PathVariable Long id) {
        return categoryService.getRequired(id);
    }

    /** List all root categories */
    @Transactional(readOnly = true)
    @GetMapping("/roots")
    public List<Category> listRoots() {
        return categoryService.listRoots();
    }

    /** List direct children of a category */
    @Transactional(readOnly = true)
    @GetMapping("/{id}/children")
    public List<Category> listChildren(@PathVariable Long id, Reader reader) {
        return categoryService.listChildren(id);
    }

    /** Resolve selection ids: parent -> [parent + children], leaf -> [id] */
    @GetMapping("/{id}/ids")
    public Map<String, Object> resolveSelection(@PathVariable Long id) {
        List<Long> ids = categoryService.resolveSelectionIds(id);
        return Map.of("categoryId", id, "effectiveIds", ids);
    }

    // ---------------- Writes ----------------

    /** Create a new category */
    @PostMapping
    public ResponseEntity<Category> create(@RequestBody CreateCategoryRequest body) {
        if (body == null || body.name == null || body.name.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        Category c = categoryService.create(body.area, body.name, body.parentId);
        return ResponseEntity.status(HttpStatus.CREATED).body(c);
    }

    /** Update an existing category */
    @PutMapping("/{id}")
    public Category update(@PathVariable Long id, @RequestBody UpdateCategoryRequest body) {
        String area = body == null ? null : body.area;
        String name = body == null ? null : body.name;
        Long parentId = body == null ? null : body.parentId;
        return categoryService.update(id, area, name, parentId);
    }

    /** Delete a category */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        categoryService.delete(id);
    }
}