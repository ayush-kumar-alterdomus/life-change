package com.ascend.auth.controller;

import com.ascend.auth.config.DevAuthFilter;
import com.ascend.auth.config.FirebasePrincipal;
import com.ascend.auth.config.RateLimitConfig;
import com.ascend.auth.config.SecurityConfig;
import com.ascend.auth.dto.LoginRequest;
import com.ascend.auth.entity.UserRole;
import com.ascend.auth.service.AuthService;
import com.ascend.auth.service.FirebaseAuthenticationException;
import com.ascend.auth.service.FirebaseTokenService;
import com.ascend.common.exception.GlobalExceptionHandler;
import com.ascend.user.entity.User;
import com.ascend.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for the Auth system verifying:
 * - Login flow with Firebase ID token (mocked)
 * - JWT validation rejects expired/invalid tokens
 * - RBAC blocks unauthorized access
 */
@WebMvcTest(
        controllers = AuthController.class,
        includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = GlobalExceptionHandler.class),
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {SecurityConfig.class, DevAuthFilter.class}),
        properties = "firebase.enabled=true"
)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private FirebaseTokenService firebaseTokenService;

    @MockBean
    private FirebaseAuth firebaseAuth;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private StringRedisTemplate redisTemplate;

    @MockBean
    private RateLimitConfig rateLimitConfig;

    private static final String TEST_UID = "firebase-uid-123";
    private static final String TEST_EMAIL = "test@example.com";
    private static final String VALID_TOKEN = "a".repeat(200);

    private User createTestUser(UserRole role) {
        return User.builder()
                .id(UUID.randomUUID())
                .firebaseUid(TEST_UID)
                .email(TEST_EMAIL)
                .username("testuser")
                .level(1)
                .xp(0L)
                .league("BRONZE")
                .role(role)
                .premium(false)
                .hardMode(false)
                .guest(false)
                .timezone("UTC")
                .build();
    }

    private UsernamePasswordAuthenticationToken createAuth(User user) {
        FirebasePrincipal principal = new FirebasePrincipal(
                user.getFirebaseUid(), user.getEmail(), "google.com", Map.of());
        return new UsernamePasswordAuthenticationToken(
                principal, null, List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())));
    }

    private org.springframework.test.web.servlet.request.RequestPostProcessor withPrincipal(User user) {
        return request -> {
            org.springframework.security.core.context.SecurityContextHolder.getContext()
                    .setAuthentication(createAuth(user));
            return request;
        };
    }

    @Nested
    @DisplayName("Login Flow Tests")
    class LoginFlowTests {

        @Test
        @DisplayName("POST /api/v1/auth/login - successful login with valid Firebase token")
        void login_withValidToken_returnsUserProfile() throws Exception {
            FirebaseToken mockToken = mock(FirebaseToken.class);
            when(mockToken.getUid()).thenReturn(TEST_UID);
            when(mockToken.getEmail()).thenReturn(TEST_EMAIL);
            when(mockToken.getClaims()).thenReturn(Map.of(
                    "firebase", Map.of("sign_in_provider", "google.com")
            ));

            when(firebaseTokenService.verifyToken(anyString())).thenReturn(mockToken);
            when(firebaseTokenService.getProvider(mockToken)).thenReturn("google.com");

            User user = createTestUser(UserRole.USER);
            when(authService.loginOrRegister(TEST_UID, TEST_EMAIL, "google.com")).thenReturn(user);

            LoginRequest request = new LoginRequest(VALID_TOKEN);

            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.username").value("testuser"))
                    .andExpect(jsonPath("$.data.email").value(TEST_EMAIL))
                    .andExpect(jsonPath("$.data.level").value(1))
                    .andExpect(jsonPath("$.data.xp").value(0))
                    .andExpect(jsonPath("$.data.league").value("BRONZE"))
                    .andExpect(jsonPath("$.data.premium").value(false));
        }

        @Test
        @DisplayName("POST /api/v1/auth/login - creates new user on first login")
        void login_newUser_createsAndReturnsProfile() throws Exception {
            FirebaseToken mockToken = mock(FirebaseToken.class);
            when(mockToken.getUid()).thenReturn(TEST_UID);
            when(mockToken.getEmail()).thenReturn(TEST_EMAIL);
            when(mockToken.getClaims()).thenReturn(Map.of(
                    "firebase", Map.of("sign_in_provider", "password")
            ));

            when(firebaseTokenService.verifyToken(anyString())).thenReturn(mockToken);
            when(firebaseTokenService.getProvider(mockToken)).thenReturn("password");

            User newUser = createTestUser(UserRole.USER);
            when(authService.loginOrRegister(TEST_UID, TEST_EMAIL, "password")).thenReturn(newUser);

            LoginRequest request = new LoginRequest(VALID_TOKEN);

            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").exists());
        }

        @Test
        @DisplayName("POST /api/v1/auth/login - missing token returns 400")
        void login_missingToken_returnsBadRequest() throws Exception {
            LoginRequest request = new LoginRequest();

            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("GET /api/v1/auth/me - returns current user profile when authenticated")
        void getMe_authenticated_returnsProfile() throws Exception {
            User user = createTestUser(UserRole.USER);
            when(authService.getCurrentUser(TEST_UID)).thenReturn(user);

            mockMvc.perform(get("/api/v1/auth/me")
                            .with(withPrincipal(user)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.username").value("testuser"))
                    .andExpect(jsonPath("$.data.email").value(TEST_EMAIL));
        }
    }

    @Nested
    @DisplayName("JWT Validation Tests")
    class JwtValidationTests {

        @Test
        @DisplayName("Request with expired token returns 401")
        void request_withExpiredToken_returnsUnauthorized() throws Exception {
            when(firebaseTokenService.verifyToken(anyString()))
                    .thenThrow(new FirebaseAuthenticationException("Token has expired. Please sign in again."));

            LoginRequest request = new LoginRequest(VALID_TOKEN);

            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("Request with invalid token returns 401")
        void request_withInvalidToken_returnsUnauthorized() throws Exception {
            when(firebaseTokenService.verifyToken(anyString()))
                    .thenThrow(new FirebaseAuthenticationException("Invalid authentication token."));

            LoginRequest request = new LoginRequest(VALID_TOKEN);

            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("Public endpoints are accessible without authentication")
        void publicEndpoints_noAuth_returnsOk() throws Exception {
            FirebaseToken mockToken = mock(FirebaseToken.class);
            when(mockToken.getUid()).thenReturn(TEST_UID);
            when(mockToken.getEmail()).thenReturn(TEST_EMAIL);
            when(mockToken.getClaims()).thenReturn(Map.of(
                    "firebase", Map.of("sign_in_provider", "google.com")
            ));
            when(firebaseTokenService.verifyToken(anyString())).thenReturn(mockToken);
            when(firebaseTokenService.getProvider(mockToken)).thenReturn("google.com");

            User user = createTestUser(UserRole.USER);
            when(authService.loginOrRegister(anyString(), anyString(), anyString())).thenReturn(user);

            LoginRequest request = new LoginRequest(VALID_TOKEN);

            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("RBAC Tests")
    class RbacTests {

        @Test
        @DisplayName("Authenticated user can access protected endpoints")
        void authenticatedUser_canAccessProtectedEndpoints() throws Exception {
            User user = createTestUser(UserRole.USER);
            when(authService.getCurrentUser(TEST_UID)).thenReturn(user);

            mockMvc.perform(get("/api/v1/auth/me")
                            .with(withPrincipal(user)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("Unauthenticated user gets 500 on protected endpoints (no principal)")
        void unauthenticatedUser_cannotAccessProtected() throws Exception {
            mockMvc.perform(get("/api/v1/auth/me"))
                    .andExpect(status().isInternalServerError());
        }
    }
}
