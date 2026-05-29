package com.ascend.auth.controller;

import com.ascend.auth.config.FirebasePrincipal;
import com.ascend.auth.dto.LoginRequest;
import com.ascend.auth.dto.LoginResponse;
import com.ascend.auth.dto.RegisterRequest;
import com.ascend.auth.dto.UserProfileResponse;
import com.ascend.auth.service.AuthService;
import com.ascend.auth.service.FirebaseTokenService;
import com.ascend.common.dto.ApiResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.auth.UserRecord;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ascend.user.entity.User;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@ConditionalOnProperty(name = "firebase.enabled", havingValue = "true")
public class AuthController {

    private final AuthService authService;
    private final FirebaseTokenService firebaseTokenService;
    private final FirebaseAuth firebaseAuth;

    public AuthController(AuthService authService,
                          FirebaseTokenService firebaseTokenService,
                          FirebaseAuth firebaseAuth) {
        this.authService = authService;
        this.firebaseTokenService = firebaseTokenService;
        this.firebaseAuth = firebaseAuth;
    }

    /**
     * POST /api/v1/auth/login
     * Accepts a Firebase ID token, validates it, finds or creates the user, and returns their profile.
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        FirebaseToken decodedToken = firebaseTokenService.verifyToken(request.getIdToken());

        String uid = decodedToken.getUid();
        String email = decodedToken.getEmail();
        String provider = firebaseTokenService.getProvider(decodedToken);

        User user = authService.loginOrRegister(uid, email, provider);

        LoginResponse loginResponse = LoginResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .level(user.getLevel())
                .xp(user.getXp())
                .league(user.getLeague())
                .premium(user.getPremium())
                .avatarUrl(user.getAvatarUrl())
                .build();

        return ResponseEntity.ok(ApiResponse.success(loginResponse));
    }

    /**
     * GET /api/v1/auth/me
     * Returns the current authenticated user's profile.
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getCurrentUser(
            @AuthenticationPrincipal FirebasePrincipal principal) {

        User user = authService.getCurrentUser(principal.uid());

        UserProfileResponse profile = UserProfileResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .avatarUrl(user.getAvatarUrl())
                .level(user.getLevel())
                .xp(user.getXp())
                .league(user.getLeague())
                .premium(user.getPremium())
                .hardMode(user.getHardMode())
                .timezone(user.getTimezone())
                .createdAt(user.getCreatedAt())
                .build();

        return ResponseEntity.ok(ApiResponse.success(profile));
    }

    /**
     * POST /api/v1/auth/register
     * Creates a Firebase user (email/password) and a corresponding PostgreSQL record.
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<LoginResponse>> register(@Valid @RequestBody RegisterRequest request) {
        try {
            // Create user in Firebase
            UserRecord.CreateRequest firebaseRequest = new UserRecord.CreateRequest()
                    .setEmail(request.getEmail())
                    .setPassword(request.getPassword())
                    .setDisplayName(request.getUsername());

            UserRecord firebaseUser = firebaseAuth.createUser(firebaseRequest);

            // Create user in PostgreSQL
            User user = authService.createUser(
                    firebaseUser.getUid(),
                    request.getEmail(),
                    request.getUsername()
            );

            LoginResponse loginResponse = LoginResponse.builder()
                    .id(user.getId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .level(user.getLevel())
                    .xp(user.getXp())
                    .league(user.getLeague())
                    .premium(user.getPremium())
                    .avatarUrl(user.getAvatarUrl())
                    .build();

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("User registered successfully", loginResponse));

        } catch (FirebaseAuthException e) {
            log.warn("Firebase user creation failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Registration failed: " + mapFirebaseError(e)));
        }
    }

    private String mapFirebaseError(FirebaseAuthException e) {
        if (e.getAuthErrorCode() == null) {
            return "Unable to create account. Please try again.";
        }
        return switch (e.getAuthErrorCode()) {
            case EMAIL_ALREADY_EXISTS -> "Email is already registered.";
            case EMAIL_NOT_FOUND -> "Email address not found.";
            case USER_DISABLED -> "This account has been disabled.";
            default -> "Unable to create account. Please try again.";
        };
    }
}
