package com.gege.activitypartner.service;

import com.gege.activitypartner.config.JwtUtil;
import com.gege.activitypartner.dto.LoginRequest;
import com.gege.activitypartner.dto.LoginResponse;
import com.gege.activitypartner.dto.RefreshTokenRequest;
import com.gege.activitypartner.dto.UserPhotoResponse;
import com.gege.activitypartner.dto.UserProfileUpdateRequest;
import com.gege.activitypartner.dto.UserRegistrationRequest;
import com.gege.activitypartner.dto.UserResponse;
import com.gege.activitypartner.dto.UserSimpleResponse;
import com.gege.activitypartner.entity.RefreshToken;
import com.gege.activitypartner.entity.User;
import com.gege.activitypartner.exception.DuplicateResourceException;
import com.gege.activitypartner.exception.ResourceNotFoundException;
import com.gege.activitypartner.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtUtil jwtUtil;
  private final RefreshTokenService refreshTokenService;
  private final FileStorageService fileStorageService;
  private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

  // Login user
  @Transactional
  public LoginResponse loginUser(LoginRequest request, HttpServletRequest httpRequest) {
    // Find user by email
    User user =
        userRepository
            .findByEmail(request.getEmail())
            .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

    // Check if user is active
    if (!user.getIsActive()) {
      throw new BadCredentialsException("Account is deactivated");
    }

    // Verify password
    if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
      throw new BadCredentialsException("Invalid email or password");
    }

    // Generate access token
    String accessToken = jwtUtil.generateToken(user.getEmail(), user.getId());

    // Generate and save refresh token
    RefreshToken refreshToken = refreshTokenService.createRefreshToken(user, httpRequest);

    // Return login response with tokens and user info
    return new LoginResponse(
        accessToken,
        refreshToken.getToken(),
        user.getId(),
        user.getEmail(),
        user.getFullName(),
        user.getProfileImageUrl(),
        user.getRating(),
        user.getBadge());
  }

  // Refresh access token using refresh token
  @Transactional
  public LoginResponse refreshToken(RefreshTokenRequest request) {
    // Find and verify refresh token
    RefreshToken refreshToken = refreshTokenService.findByToken(request.getRefreshToken());
    refreshToken = refreshTokenService.verifyExpiration(refreshToken);

    User user = refreshToken.getUser();

    // Generate new access token
    String newAccessToken = jwtUtil.generateToken(user.getEmail(), user.getId());

    // Return response with new access token and same refresh token
    return new LoginResponse(
        newAccessToken,
        refreshToken.getToken(),
        user.getId(),
        user.getEmail(),
        user.getFullName(),
        user.getProfileImageUrl(),
        user.getRating(),
        user.getBadge());
  }

  // Logout user (deactivate refresh token)
  @Transactional
  public void logout(String refreshToken) {
    refreshTokenService.deactivateToken(refreshToken);
  }

  // Logout from all devices
  @Transactional
  public void logoutAll(Long userId) {
    refreshTokenService.deactivateAllUserTokens(userId);
  }

  // Register new user
  @Transactional
  public LoginResponse registerUser(
      UserRegistrationRequest request, HttpServletRequest httpRequest) {
    // Check if email already exists
    if (userRepository.existsByEmail(request.getEmail())) {
      throw new DuplicateResourceException("Email already registered");
    }

    // Validate age (must be 18 or older)
    int age = Period.between(request.getBirthDate(), LocalDate.now()).getYears();
    if (age < 18) {
      throw new IllegalArgumentException("You must be at least 18 years old to register");
    }

    User user = new User();
    user.setFullName(request.getFullName());
    user.setEmail(request.getEmail());
    user.setPassword(passwordEncoder.encode(request.getPassword()));
    user.setBirthDate(request.getBirthDate());
    user.setRating(0.0);
    user.setCompletedActivities(0);
    user.setIsActive(true);

    User savedUser = userRepository.save(user);

    // Generate access token
    String accessToken = jwtUtil.generateToken(savedUser.getEmail(), savedUser.getId());

    // Generate and save refresh token
    RefreshToken refreshToken = refreshTokenService.createRefreshToken(savedUser, httpRequest);

    // Return login response with tokens and user info
    return new LoginResponse(
        accessToken,
        refreshToken.getToken(),
        savedUser.getId(),
        savedUser.getEmail(),
        savedUser.getFullName(),
        savedUser.getProfileImageUrl(),
        savedUser.getRating(),
        savedUser.getBadge());
  }

  // Get user by ID
  public UserResponse getUserById(Long id) {
    User user =
        userRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    return convertToResponse(user);
  }

  // Get user by email
  public UserResponse getUserByEmail(String email) {
    User user =
        userRepository
            .findByEmail(email)
            .orElseThrow(
                () -> new ResourceNotFoundException("User not found with email: " + email));
    return convertToResponse(user);
  }

  // Update user profile
  @Transactional
  public UserResponse updateUserProfile(Long id, UserProfileUpdateRequest request) {
    User user =
        userRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

    if (request.getFullName() != null) {
      user.setFullName(request.getFullName());
    }
    if (request.getBio() != null) {
      user.setBio(request.getBio());
    }
    if (request.getProfileImageUrl() != null) {
      user.setProfileImageUrl(request.getProfileImageUrl());
    }
    if (request.getCity() != null) {
      user.setCity(request.getCity());
    }
    if (request.getPlaceId() != null) {
      user.setPlaceId(request.getPlaceId());
    }
    if (request.getLatitude() != null) {
      user.setLatitude(request.getLatitude());
    }
    if (request.getLongitude() != null) {
      user.setLongitude(request.getLongitude());
    }
    if (request.getInterests() != null) {
      user.setInterests(request.getInterests());
    }

    User updatedUser = userRepository.save(user);
    return convertToResponse(updatedUser);
  }

  // Update user location (city name, placeId, and coordinates)
  @Transactional
  public UserResponse updateUserLocation(
      Long id, String city, String placeId, BigDecimal latitude, BigDecimal longitude) {
    User user =
        userRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

    user.setCity(city);
    user.setPlaceId(placeId);
    user.setLatitude(latitude);
    user.setLongitude(longitude);

    User updatedUser = userRepository.save(user);
    return convertToResponse(updatedUser);
  }

  // Get all active users
  public List<UserResponse> getAllActiveUsers() {
    return userRepository.findByIsActiveTrue().stream()
        .map(this::convertToResponse)
        .collect(Collectors.toList());
  }

  // Search users by name
  public List<UserResponse> searchUsersByName(String name) {
    return userRepository.searchByName(name).stream()
        .map(this::convertToResponse)
        .collect(Collectors.toList());
  }

  // Find users by interest
  public List<UserSimpleResponse> getUsersByInterest(String interest) {
    return userRepository.findByInterest(interest).stream()
        .map(this::convertToSimpleResponse)
        .collect(Collectors.toList());
  }

  // Get top rated users
  public List<UserSimpleResponse> getTopRatedUsers() {
    return userRepository.findTopRatedUsers().stream()
        .limit(10)
        .map(this::convertToSimpleResponse)
        .collect(Collectors.toList());
  }

  // Update user rating (called after activity completion)
  @Transactional
  public void updateUserRating(Long userId, Double newRating) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
    user.setRating(newRating);
    userRepository.save(user);
  }

  // Increment completed activities
  @Transactional
  public void incrementCompletedActivities(Long userId) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
    user.setCompletedActivities(user.getCompletedActivities() + 1);

    // Award badges based on milestones
    if (user.getCompletedActivities() == 10) {
      user.setBadge("⭐");
    } else if (user.getCompletedActivities() == 50) {
      user.setBadge("👑");
    } else if (user.getCompletedActivities() == 100) {
      user.setBadge("💎");
    }

    userRepository.save(user);
  }

  // Deactivate user
  @Transactional
  public void deactivateUser(Long id) {
    User user =
        userRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    user.setIsActive(false);
    userRepository.save(user);
  }

  // Update profile image
  @Transactional
  public String updateProfileImage(Long userId, MultipartFile file) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

    // Delete old profile image if it exists
    if (user.getProfileImageUrl() != null && !user.getProfileImageUrl().isEmpty()) {
      try {
        // Extract filename from URL (assumes format: /api/users/images/{filename})
        String oldFileName =
            user.getProfileImageUrl().substring(user.getProfileImageUrl().lastIndexOf('/') + 1);
        fileStorageService.deleteFile(oldFileName);
      } catch (Exception e) {
        // Log error but continue with upload
        System.err.println("Failed to delete old profile image: " + e.getMessage());
      }
    }

    // Store the new file
    String fileName = fileStorageService.storeFile(file);

    // Generate URL for the uploaded file
    String fileUrl = "/api/users/images/" + fileName;

    // Update user's profile image URL
    user.setProfileImageUrl(fileUrl);
    userRepository.save(user);

    // Return only the URL string
    return fileUrl;
  }

  // Convert User entity to UserResponse DTO
  private UserResponse convertToResponse(User user) {
    UserResponse response = new UserResponse();
    response.setId(user.getId());
    response.setFullName(user.getFullName());
    response.setEmail(user.getEmail());
    response.setBio(user.getBio());
    response.setProfileImageUrl(user.getProfileImageUrl());
    response.setRating(user.getRating());
    response.setCompletedActivities(user.getCompletedActivities());
    response.setCity(user.getCity());
    response.setPlaceId(user.getPlaceId());
    response.setLatitude(user.getLatitude() != null ? user.getLatitude().doubleValue() : null);
    response.setLongitude(user.getLongitude() != null ? user.getLongitude().doubleValue() : null);
    response.setInterests(user.getInterests());
    response.setBadge(user.getBadge());

    // Map photos if they exist
    if (user.getPhotos() != null && !user.getPhotos().isEmpty()) {
      response.setPhotos(
          user.getPhotos().stream()
              .map(
                  photo -> {
                    UserPhotoResponse photoResponse = new UserPhotoResponse();
                    photoResponse.setId(photo.getId());
                    photoResponse.setPhotoUrl(photo.getPhotoUrl());
                    photoResponse.setIsProfilePicture(photo.getIsProfilePicture());
                    photoResponse.setDisplayOrder(photo.getDisplayOrder());
                    photoResponse.setUploadedAt(photo.getUploadedAt());
                    return photoResponse;
                  })
              .collect(Collectors.toList()));
    }

    response.setCreatedAt(user.getCreatedAt().format(DATE_FORMATTER));
    return response;
  }

  // Convert User entity to UserSimpleResponse DTO
  private UserSimpleResponse convertToSimpleResponse(User user) {
    UserSimpleResponse response = new UserSimpleResponse();
    response.setId(user.getId());
    response.setFullName(user.getFullName());
    response.setProfileImageUrl(user.getProfileImageUrl());
    response.setRating(user.getRating());
    response.setBadge(user.getBadge());
    return response;
  }

  // Get FileStorageService (used by controller to serve files)
  public FileStorageService getFileStorageService() {
    return fileStorageService;
  }
}
