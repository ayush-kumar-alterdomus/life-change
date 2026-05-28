package com.ascend.auth.entity;

/**
 * Defines the role hierarchy for the application.
 * Higher roles inherit permissions from lower roles.
 */
public enum UserRole {
    USER,
    PREMIUM_USER,
    MODERATOR,
    ADMIN,
    SUPER_ADMIN
}
