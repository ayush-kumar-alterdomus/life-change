package com.ascend.auth.config;

import com.ascend.user.entity.User;
import com.ascend.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Aspect that enforces the {@link GuestRestricted} annotation.
 * Blocks guest users from accessing endpoints annotated with @GuestRestricted.
 *
 * A user is considered a guest if:
 * - Their Firebase provider is "anonymous", OR
 * - Their user record in the database has isGuest=true
 *
 * Restricted features: leaderboard access, guild features, cloud sync.
 */
@Slf4j
@Aspect
@Component
public class GuestRestrictionAspect {

    private static final String ANONYMOUS_PROVIDER = "anonymous";
    private static final String GUEST_DENIED_MESSAGE =
            "Guest users cannot access this feature. Please create an account.";

    private final UserRepository userRepository;

    public GuestRestrictionAspect(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Around("@annotation(guestRestricted)")
    public Object checkGuestRestriction(ProceedingJoinPoint joinPoint, GuestRestricted guestRestricted) throws Throwable {
        enforceGuestRestriction();
        return joinPoint.proceed();
    }

    @Around("@within(guestRestricted)")
    public Object checkGuestRestrictionOnClass(ProceedingJoinPoint joinPoint, GuestRestricted guestRestricted) throws Throwable {
        enforceGuestRestriction();
        return joinPoint.proceed();
    }

    private void enforceGuestRestriction() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Authentication required");
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof FirebasePrincipal firebasePrincipal)) {
            return;
        }

        // Check provider first (fast path — no DB call needed)
        if (ANONYMOUS_PROVIDER.equals(firebasePrincipal.provider())) {
            log.warn("Guest user uid={} denied access to restricted feature", firebasePrincipal.uid());
            throw new AccessDeniedException(GUEST_DENIED_MESSAGE);
        }

        // Fallback: check the database isGuest flag
        Optional<User> userOpt = userRepository.findByFirebaseUid(firebasePrincipal.uid());
        if (userOpt.isPresent() && Boolean.TRUE.equals(userOpt.get().getGuest())) {
            log.warn("Guest user uid={} (db flag) denied access to restricted feature", firebasePrincipal.uid());
            throw new AccessDeniedException(GUEST_DENIED_MESSAGE);
        }
    }
}
