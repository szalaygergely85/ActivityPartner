package com.gege.activitypartner.service;

import com.gege.activitypartner.dto.CategoryRequest;
import com.gege.activitypartner.dto.CategoryResponse;
import com.gege.activitypartner.dto.CategoryUpdateRequest;
import com.gege.activitypartner.entity.Category;
import com.gege.activitypartner.exception.DuplicateResourceException;
import com.gege.activitypartner.exception.ResourceNotFoundException;
import com.gege.activitypartner.repository.CategoryRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryService {

  private final CategoryRepository categoryRepository;

  // Create new category (admin)
  public CategoryResponse createCategory(CategoryRequest request) {
    // Check if category already exists
    if (categoryRepository.existsByName(request.getName())) {
      throw new DuplicateResourceException(
          "Category already exists with name: " + request.getName());
    }

    Category category = new Category();
    category.setName(request.getName());
    category.setDescription(request.getDescription());
    category.setIcon(request.getIcon());
    category.setImageResourceName(request.getImageResourceName());
    category.setIsActive(true);
    category.setActivityCount(0);

    Category saved = categoryRepository.save(category);
    return mapToResponse(saved);
  }

  // Get all active categories
  @Transactional(readOnly = true)
  public List<CategoryResponse> getAllActiveCategories() {
    return categoryRepository.findByIsActiveTrueOrderByNameAsc().stream()
        .map(this::mapToResponse)
        .collect(Collectors.toList());
  }

  // Get popular categories (ordered by activity count)
  @Transactional(readOnly = true)
  public List<CategoryResponse> getPopularCategories() {
    return categoryRepository.findPopularCategories().stream()
        .map(this::mapToResponse)
        .collect(Collectors.toList());
  }

  // Get category by id
  @Transactional(readOnly = true)
  public CategoryResponse getCategoryById(Long id) {
    Category category =
        categoryRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
    return mapToResponse(category);
  }

  // Update category (admin)
  public CategoryResponse updateCategory(Long id, CategoryUpdateRequest request) {
    Category category =
        categoryRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

    // Check if name is being changed and if it already exists
    if (request.getName() != null && !request.getName().equals(category.getName())) {
      if (categoryRepository.existsByName(request.getName())) {
        throw new DuplicateResourceException(
            "Category already exists with name: " + request.getName());
      }
      category.setName(request.getName());
    }

    if (request.getDescription() != null) {
      category.setDescription(request.getDescription());
    }

    if (request.getIcon() != null) {
      category.setIcon(request.getIcon());
    }

    if (request.getImageResourceName() != null) {
      category.setImageResourceName(request.getImageResourceName());
    }

    if (request.getIsActive() != null) {
      category.setIsActive(request.getIsActive());
    }

    Category updated = categoryRepository.save(category);
    return mapToResponse(updated);
  }

  // Deactivate category (admin) - soft delete
  public void deactivateCategory(Long id) {
    Category category =
        categoryRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

    category.setIsActive(false);
    categoryRepository.save(category);
  }

  // Increment activity count (called when activity is created)
  public void incrementActivityCount(String categoryName) {
    categoryRepository
        .findByName(categoryName)
        .ifPresent(
            category -> {
              category.setActivityCount(category.getActivityCount() + 1);
              categoryRepository.save(category);
            });
  }

  // Decrement activity count (called when activity is deleted)
  public void decrementActivityCount(String categoryName) {
    categoryRepository
        .findByName(categoryName)
        .ifPresent(
            category -> {
              if (category.getActivityCount() > 0) {
                category.setActivityCount(category.getActivityCount() - 1);
                categoryRepository.save(category);
              }
            });
  }

  // Mapping helper
  private CategoryResponse mapToResponse(Category category) {
    CategoryResponse response = new CategoryResponse();
    response.setId(category.getId());
    response.setName(category.getName());
    response.setDescription(category.getDescription());
    response.setIcon(category.getIcon());
    response.setImageResourceName(category.getImageResourceName());
    response.setIsActive(category.getIsActive());
    response.setActivityCount(category.getActivityCount());
    response.setCreatedAt(category.getCreatedAt());
    return response;
  }
}
