package com.ascend.auth.config;

import com.ascend.auth.entity.UserRole;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Aspect that enforces the {@link RequireRole} annotation.
 * Checks that the current authenticated user has one of the required roles.
 */
@Slf4j
@Aspect
@Component
public class RoleCheckAspect {

    @Around("@annotation(requireRole)")
    public Object checkRole(ProceedingJoinPoint joinPoint, RequireRole requireRole) throws Throwable {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Authentication required");
        }

        Set<String> requiredRoles = Arrays.stream(requireRole.value())
                .map(role -> "ROLE_" + role.name())
                .collect(Collectors.toSet());

        Set<String> userRoles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        boolean hasRequiredRole = requiredRoles.stream().anyMatch(userRoles::contains);

        if (!hasRequiredRole) {
            log.warn("Access denied for user {} - required roles: {}, user roles: {}",
                    authentication.getName(), requiredRoles, userRoles);
            throw new AccessDeniedException("Insufficient permissions");
        }

        return joinPoint.proceed();
    }

    @Around("@within(requireRole)")
    public Object checkRoleOnClass(ProceedingJoinPoint joinPoint, RequireRole requireRole) throws Throwable {
        return checkRole(joinPoint, requireRole);
    }
}
