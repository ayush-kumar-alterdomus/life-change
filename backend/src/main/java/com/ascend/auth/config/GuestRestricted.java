package com.ascend.auth.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom annotation that marks endpoints as off-limits for guest users.
 * Guest users (anonymous Firebase auth) cannot access methods or classes
 * annotated with this annotation.
 *
 * Restricted features include: leaderboard access, guild features, and cloud sync.
 *
 * Usage:
 * <pre>
 * {@code @GuestRestricted}
 * public void socialFeatureMethod() { ... }
 * </pre>
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface GuestRestricted {
}
