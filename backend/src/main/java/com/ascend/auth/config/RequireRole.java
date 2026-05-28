package com.ascend.auth.config;

import com.ascend.auth.entity.UserRole;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom annotation for method-level role-based access control.
 * Methods annotated with this will only be accessible to users
 * with one of the specified roles.
 *
 * Usage:
 * <pre>
 * {@code @RequireRole({UserRole.ADMIN, UserRole.SUPER_ADMIN})}
 * public void adminOnlyMethod() { ... }
 * </pre>
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireRole {
    UserRole[] value();
}
