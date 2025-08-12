package com.bell_ringer.controllers;

import com.bell_ringer.models.Category;
import com.bell_ringer.services.CategoryService;
import com.bell_ringer.services.dto.CategoryDto;
import com.bell_ringer.services.dto.CategoryRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    // ---------------- Reads ----------------

    /** Get a category by id */
    @Transactional(readOnly = true)
    @GetMapping("/{id}")
    public CategoryDto getById(@PathVariable Long id) {
        return categoryService.getRequiredDto(id);
    }

    /** List all root categories */
    @Transactional(readOnly = true)
    @GetMapping("/roots")
    public List<CategoryDto> listRoots() {
        return categoryService.listRootsDto();
    }

    /** List direct children of a category */
    @Transactional(readOnly = true)
    @GetMapping("/{id}/children")
    public List<CategoryDto> listChildren(@PathVariable Long id) {
        return categoryService.listChildrenDto(id);
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
    public ResponseEntity<CategoryDto> create(@Valid @RequestBody CategoryRequest.Create body) {
        CategoryDto c = categoryService.createDto(body.area(), body.name(), body.parentId());
        return ResponseEntity.status(HttpStatus.CREATED).body(c);
    }

    /** Update an existing category */
    @PutMapping("/{id}")
    public Category update(@PathVariable Long id, @RequestBody CategoryRequest.Update body) {
        String area = body == null ? null : body.area();
        String name = body == null ? null : body.name();
        Long parentId = body == null ? null : body.parentId();
        return categoryService.update(id, area, name, parentId);
    }

    /** Delete a category */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        categoryService.delete(id);
    }
}