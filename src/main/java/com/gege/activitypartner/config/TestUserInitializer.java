package com.gege.activitypartner.config;

import com.gege.activitypartner.entity.User;
import com.gege.activitypartner.repository.UserRepository;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Initializes a test user for Google Play review purposes. The test user credentials are configured
 * via environment variables for security.
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class TestUserInitializer {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  @Value("${test.user.enabled:false}")
  private boolean testUserEnabled;

  @Value("${test.user.email:}")
  private String testUserEmail;

  @Value("${test.user.password:}")
  private String testUserPassword;

  @Bean
  CommandLineRunner initTestUser() {
    return args -> {
      if (!testUserEnabled) {
        log.debug("Test user creation is disabled");
        return;
      }

      if (testUserEmail.isEmpty() || testUserPassword.isEmpty()) {
        log.warn("Test user enabled but email or password not configured");
        return;
      }

      // Check if test user already exists
      if (userRepository.existsByEmail(testUserEmail)) {
        log.info("Test user already exists: {}", testUserEmail);
        return;
      }

      // Create test user
      User testUser = new User();
      testUser.setFullName("Google Play Reviewer");
      testUser.setEmail(testUserEmail);
      testUser.setPassword(passwordEncoder.encode(testUserPassword));
      testUser.setBirthDate(LocalDate.of(1995, 1, 15)); // 29 years old
      testUser.setBio("Test account for Google Play app review");
      testUser.setRating(4.5);
      testUser.setCompletedActivities(10);
      testUser.setIsActive(true);
      testUser.setNotificationsEnabled(true);

      userRepository.save(testUser);
      log.info("Test user created successfully: {}", testUserEmail);
    };
  }
}
