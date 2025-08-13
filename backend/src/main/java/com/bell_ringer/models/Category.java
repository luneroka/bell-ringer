package com.bell_ringer.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.text.Normalizer;
import java.time.OffsetDateTime;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

@Entity
@Table(name = "categories", uniqueConstraints = {
        @UniqueConstraint(name = "uk_categories_slug", columnNames = { "slug" }),
        @UniqueConstraint(name = "uk_parent_name", columnNames = { "parent_id", "name" })
}, indexes = {
        @Index(name = "idx_categories_slug", columnList = "slug"),
        @Index(name = "idx_categories_parent", columnList = "parent_id")
})
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 150)
    @Column(name = "name", nullable = false)
    private String name;

    @NotBlank
    @Size(max = 200)
    @Column(name = "slug", nullable = false)
    private String slug;

    // Self-reference to support parent → children hierarchy
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Category parent;

    @JsonIgnore
    @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = false)
    private Set<Category> children = new LinkedHashSet<>();

    // Reverse side to questions (each Question has @ManyToOne Category)
    @JsonIgnore
    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
    private Set<Question> questions = new LinkedHashSet<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    public Category() {
    }

    public Category(String name, Category parent) {
        this.name = name;
        this.parent = parent;
        this.slug = buildSlug(name, parent);
    }

    @PrePersist
    public void prePersist() {
        if (this.slug == null || this.slug.isBlank()) {
            this.slug = buildSlug(name, parent);
        }
    }

    @PreUpdate
    public void preUpdate() {
        if (this.slug == null || this.slug.isBlank()) {
            this.slug = buildSlug(name, parent);
        }
    }

    // Helpers to maintain both sides of the parent/children relation
    public void addChild(Category child) {
        children.add(child);
        child.setParent(this);
    }

    public void removeChild(Category child) {
        children.remove(child);
        child.setParent(null);
    }

    /**
     * Slug builder: parent name (if present) + name → URL-friendly.
     * Example: parent.name="Frontend", name="React Hooks" → "frontend-react-hooks"
     * For root categories: just the name → "frontend"
     */
    private static String buildSlug(String name, Category parent) {
        String base;
        if (parent != null) {
            base = String.join("-", safe(parent.getName()), safe(name));
        } else {
            base = safe(name);
        }
        return base.replaceAll("-{2,}", "-")
                .replaceAll("(^-|-$)", "")
                .toLowerCase(Locale.ROOT);
    }

    private static String safe(String s) {
        if (s == null)
            return "";
        String x = s.trim();
        String n = Normalizer.normalize(x, Normalizer.Form.NFD);
        return n.replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .replaceAll("[^a-zA-Z0-9]+", "-");
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public Category getParent() {
        return parent;
    }

    public void setParent(Category parent) {
        this.parent = parent;
    }

    public Set<Category> getChildren() {
        return children;
    }

    public void setChildren(Set<Category> children) {
        this.children = children;
    }

    public Set<Question> getQuestions() {
        return questions;
    }

    public void setQuestions(Set<Question> questions) {
        this.questions = questions;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
