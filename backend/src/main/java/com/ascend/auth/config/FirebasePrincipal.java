package com.ascend.auth.config;

import java.io.Serializable;
import java.util.Map;

/**
 * Represents the authenticated Firebase user principal stored in the SecurityContext.
 * Contains the essential identity fields extracted from the verified Firebase token.
 */
public record FirebasePrincipal(
        String uid,
        String email,
        String provider,
        Map<String, Object> claims
) implements Serializable {

    @Override
    public String toString() {
        return "FirebasePrincipal{uid='%s', email='%s', provider='%s'}".formatted(uid, email, provider);
    }
}
