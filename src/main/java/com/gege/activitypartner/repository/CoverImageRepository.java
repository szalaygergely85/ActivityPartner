package com.gege.activitypartner.repository;

import com.gege.activitypartner.entity.CoverImage;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CoverImageRepository extends JpaRepository<CoverImage, Long> {

  List<CoverImage> findByActiveTrue();

  Optional<CoverImage> findByFileName(String fileName);

  List<CoverImage> findByActiveTrueOrderByDisplayNameAsc();
}
