package com.gege.activitypartner.controller;

import com.gege.activitypartner.dto.LoginRequest;
import com.gege.activitypartner.dto.LoginResponse;
import com.gege.activitypartner.dto.RefreshTokenRequest;
import com.gege.activitypartner.dto.UserProfileUpdateRequest;
import com.gege.activitypartner.dto.UserRegistrationRequest;
import com.gege.activitypartner.dto.UserResponse;
import com.gege.activitypartner.dto.UserSimpleResponse;
import com.gege.activitypartner.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // Configure CORS as needed
public class UserController {

    private final UserService userService;

    // Login endpoint
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        LoginResponse response = userService.loginUser(request, httpRequest);
        return ResponseEntity.ok(response);
    }

    // Refresh token endpoint
    @PostMapping("/refresh-token")
    public ResponseEntity<LoginResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        LoginResponse response = userService.refreshToken(request);
        return ResponseEntity.ok(response);
    }

    // Logout endpoint (single device)
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshTokenRequest request) {
        userService.logout(request.getRefreshToken());
        return ResponseEntity.ok().build();
    }

    // Logout from all devices
    @PostMapping("/logout-all/{userId}")
    public ResponseEntity<Void> logoutAll(@PathVariable Long userId) {
        userService.logoutAll(userId);
        return ResponseEntity.ok().build();
    }

    // Register new user
    @PostMapping("/register")
    public ResponseEntity<UserResponse> registerUser(@Valid @RequestBody UserRegistrationRequest request) {
        UserResponse user = userService.registerUser(request);
        return new ResponseEntity<>(user, HttpStatus.CREATED);
    }

    // Get user by ID
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        UserResponse user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    // Get user by email
    @GetMapping("/email/{email}")
    public ResponseEntity<UserResponse> getUserByEmail(@PathVariable String email) {
        UserResponse user = userService.getUserByEmail(email);
        return ResponseEntity.ok(user);
    }

    // Update user profile
    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUserProfile(
            @PathVariable Long id,
            @Valid @RequestBody UserProfileUpdateRequest request) {
        UserResponse user = userService.updateUserProfile(id, request);
        return ResponseEntity.ok(user);
    }

    // Get all active users
    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllActiveUsers() {
        List<UserResponse> users = userService.getAllActiveUsers();
        return ResponseEntity.ok(users);
    }

    // Search users by name
    @GetMapping("/search")
    public ResponseEntity<List<UserResponse>> searchUsers(@RequestParam String name) {
        List<UserResponse> users = userService.searchUsersByName(name);
        return ResponseEntity.ok(users);
    }

    // Get users by interest
    @GetMapping("/interest/{interest}")
    public ResponseEntity<List<UserSimpleResponse>> getUsersByInterest(@PathVariable String interest) {
        List<UserSimpleResponse> users = userService.getUsersByInterest(interest);
        return ResponseEntity.ok(users);
    }

    // Get top rated users
    @GetMapping("/top-rated")
    public ResponseEntity<List<UserSimpleResponse>> getTopRatedUsers() {
        List<UserSimpleResponse> users = userService.getTopRatedUsers();
        return ResponseEntity.ok(users);
    }

    // Deactivate user
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivateUser(@PathVariable Long id) {
        userService.deactivateUser(id);
        return ResponseEntity.noContent().build();
    }
}
