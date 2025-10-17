package com.gege.activitypartner.service;

import com.gege.activitypartner.config.JwtUtil;
import com.gege.activitypartner.dto.LoginRequest;
import com.gege.activitypartner.dto.LoginResponse;
import com.gege.activitypartner.dto.RefreshTokenRequest;
import com.gege.activitypartner.dto.UserProfileUpdateRequest;
import com.gege.activitypartner.dto.UserRegistrationRequest;
import com.gege.activitypartner.dto.UserResponse;
import com.gege.activitypartner.dto.UserSimpleResponse;
import com.gege.activitypartner.entity.RefreshToken;
import com.gege.activitypartner.entity.User;
import com.gege.activitypartner.exception.ResourceNotFoundException;
import com.gege.activitypartner.exception.DuplicateResourceException;
import com.gege.activitypartner.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    // Login user
    @Transactional
    public LoginResponse loginUser(LoginRequest request, HttpServletRequest httpRequest) {
        // Find user by email
        User user = userRepository.findByEmail(request.getEmail())
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
                user.getBadge()
        );
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
                user.getBadge()
        );
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
    public UserResponse registerUser(UserRegistrationRequest request) {
        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already registered");
        }

        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRating(0.0);
        user.setCompletedActivities(0);
        user.setIsActive(true);

        User savedUser = userRepository.save(user);
        return convertToResponse(savedUser);
    }

    // Get user by ID
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return convertToResponse(user);
    }

    // Get user by email
    public UserResponse getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        return convertToResponse(user);
    }

    // Update user profile
    @Transactional
    public UserResponse updateUserProfile(Long id, UserProfileUpdateRequest request) {
        User user = userRepository.findById(id)
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
        if (request.getInterests() != null) {
            user.setInterests(request.getInterests());
        }

        User updatedUser = userRepository.save(user);
        return convertToResponse(updatedUser);
    }

    // Get all active users
    public List<UserResponse> getAllActiveUsers() {
        return userRepository.findByIsActiveTrue()
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    // Search users by name
    public List<UserResponse> searchUsersByName(String name) {
        return userRepository.searchByName(name)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    // Find users by interest
    public List<UserSimpleResponse> getUsersByInterest(String interest) {
        return userRepository.findByInterest(interest)
                .stream()
                .map(this::convertToSimpleResponse)
                .collect(Collectors.toList());
    }

    // Get top rated users
    public List<UserSimpleResponse> getTopRatedUsers() {
        return userRepository.findTopRatedUsers()
                .stream()
                .limit(10)
                .map(this::convertToSimpleResponse)
                .collect(Collectors.toList());
    }

    // Update user rating (called after activity completion)
    @Transactional
    public void updateUserRating(Long userId, Double newRating) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        user.setRating(newRating);
        userRepository.save(user);
    }

    // Increment completed activities
    @Transactional
    public void incrementCompletedActivities(Long userId) {
        User user = userRepository.findById(userId)
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
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        user.setIsActive(false);
        userRepository.save(user);
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
        response.setInterests(user.getInterests());
        response.setBadge(user.getBadge());
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
}
