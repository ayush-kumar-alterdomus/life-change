package com.ascend.auth.service;

import lombok.Getter;

/**
 * Exception thrown when Firebase token verification fails.
 * Wraps FirebaseAuthException with user-friendly error messages
 * that avoid leaking internal details.
 */
@Getter
public class FirebaseAuthenticationException extends RuntimeException {

    public FirebaseAuthenticationException(String message) {
        super(message);
    }

    public FirebaseAuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}
