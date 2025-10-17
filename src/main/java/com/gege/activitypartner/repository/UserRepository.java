package com.gege.activitypartner.repository;

import com.gege.activitypartner.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findByIsActiveTrue();

    // Find users by interest
    @Query("SELECT u FROM User u JOIN u.interests i WHERE i = :interest AND u.isActive = true")
    List<User> findByInterest(@Param("interest") String interest);

    // Find top rated users
    @Query("SELECT u FROM User u WHERE u.isActive = true ORDER BY u.rating DESC")
    List<User> findTopRatedUsers();

    // Search users by name
    @Query("SELECT u FROM User u WHERE LOWER(u.fullName) LIKE LOWER(CONCAT('%', :name, '%')) AND u.isActive = true")
    List<User> searchByName(@Param("name") String name);
}
