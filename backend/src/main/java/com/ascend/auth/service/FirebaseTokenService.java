package com.ascend.auth.service;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
public class FirebaseTokenService {

    private final FirebaseAuth firebaseAuth;

    public FirebaseTokenService(FirebaseAuth firebaseAuth) {
        this.firebaseAuth = firebaseAuth;
    }

    /**
     * Verifies a Firebase ID token and returns the decoded token.
     *
     * @param idToken the Firebase ID token string from the client
     * @return the decoded FirebaseToken containing uid, email, provider, and custom claims
     * @throws FirebaseAuthenticationException if the token is invalid, expired, or revoked
     */
    public FirebaseToken verifyToken(String idToken) {
        try {
            FirebaseToken decodedToken = firebaseAuth.verifyIdToken(idToken);
            log.debug("Token verified for uid: {}", decodedToken.getUid());
            return decodedToken;
        } catch (FirebaseAuthException e) {
            log.warn("Firebase token verification failed: {} (code: {})", e.getMessage(), e.getAuthErrorCode());
            throw new FirebaseAuthenticationException(mapErrorMessage(e), e);
        }
    }

    /**
     * Extracts the sign-in provider from a decoded Firebase token.
     *
     * @param token the decoded FirebaseToken
     * @return the provider ID (e.g., "google.com", "apple.com", "password", "anonymous")
     */
    public String getProvider(FirebaseToken token) {
        Map<String, Object> claims = token.getClaims();
        Object firebase = claims.get("firebase");
        if (firebase instanceof Map<?, ?> firebaseMap) {
            Object signInProvider = firebaseMap.get("sign_in_provider");
            if (signInProvider != null) {
                return signInProvider.toString();
            }
        }
        return "unknown";
    }

    private String mapErrorMessage(FirebaseAuthException e) {
        return switch (e.getAuthErrorCode()) {
            case EXPIRED_ID_TOKEN -> "Token has expired. Please sign in again.";
            case REVOKED_ID_TOKEN -> "Token has been revoked. Please sign in again.";
            case INVALID_ID_TOKEN -> "Invalid authentication token.";
            default -> "Authentication failed. Please try again.";
        };
    }
}
