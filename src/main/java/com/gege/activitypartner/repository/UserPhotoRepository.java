package com.gege.activitypartner.repository;

import com.gege.activitypartner.entity.UserPhoto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserPhotoRepository extends JpaRepository<UserPhoto, Long> {

    // Find all photos for a user ordered by display order
    List<UserPhoto> findByUserIdOrderByDisplayOrder(Long userId);

    // Find the profile picture for a user
    Optional<UserPhoto> findByUserIdAndIsProfilePictureTrue(Long userId);

    // Count photos for a user
    long countByUserId(Long userId);

    // Delete all photos for a user
    void deleteByUserId(Long userId);
}
