package com.gege.activitypartner.controller;

import com.gege.activitypartner.dto.CategoryRequest;
import com.gege.activitypartner.dto.CategoryResponse;
import com.gege.activitypartner.dto.CategoryUpdateRequest;
import com.gege.activitypartner.service.CategoryService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // Configure properly in production
public class CategoryController {

  private final CategoryService categoryService;

  // Create new category (admin)
  @PostMapping
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<CategoryResponse> createCategory(
      @Valid @RequestBody CategoryRequest request) {
    CategoryResponse response = categoryService.createCategory(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  // Get all active categories
  @GetMapping
  public ResponseEntity<List<CategoryResponse>> getAllActiveCategories() {
    List<CategoryResponse> categories = categoryService.getAllActiveCategories();
    return ResponseEntity.ok(categories);
  }

  // Get popular categories (ordered by activity count)
  @GetMapping("/popular")
  public ResponseEntity<List<CategoryResponse>> getPopularCategories() {
    List<CategoryResponse> categories = categoryService.getPopularCategories();
    return ResponseEntity.ok(categories);
  }

  // Get category by id
  @GetMapping("/{id}")
  public ResponseEntity<CategoryResponse> getCategoryById(@PathVariable Long id) {
    CategoryResponse response = categoryService.getCategoryById(id);
    return ResponseEntity.ok(response);
  }

  // Update category (admin)
  @PutMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<CategoryResponse> updateCategory(
      @PathVariable Long id, @Valid @RequestBody CategoryUpdateRequest request) {
    CategoryResponse response = categoryService.updateCategory(id, request);
    return ResponseEntity.ok(response);
  }

  // Deactivate category (admin) - soft delete
  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Void> deactivateCategory(@PathVariable Long id) {
    categoryService.deactivateCategory(id);
    return ResponseEntity.noContent().build();
  }
}
