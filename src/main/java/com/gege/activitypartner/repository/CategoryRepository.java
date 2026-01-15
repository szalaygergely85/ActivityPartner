package com.gege.activitypartner.repository;

import com.gege.activitypartner.entity.Category;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

  // Find category by name
  Optional<Category> findByName(String name);

  // Check if category exists by name
  boolean existsByName(String name);

  // Get all active categories
  List<Category> findByIsActiveTrue();

  // Get popular categories ordered by activity count
  @Query("SELECT c FROM Category c WHERE c.isActive = true ORDER BY c.activityCount DESC")
  List<Category> findPopularCategories();

  // Get categories ordered by name
  List<Category> findByIsActiveTrueOrderByNameAsc();
}
