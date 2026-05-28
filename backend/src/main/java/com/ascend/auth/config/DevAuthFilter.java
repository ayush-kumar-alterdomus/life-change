package com.ascend.auth.config;

import com.ascend.auth.entity.UserRole;
import com.ascend.user.entity.User;
import com.ascend.user.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Dev-only filter that auto-authenticates all requests with a dev user.
 * Only active when firebase.enabled=false.
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "firebase.enabled", havingValue = "false", matchIfMissing = true)
public class DevAuthFilter extends OncePerRequestFilter {

    private static final String DEV_UID = "dev-user-uid";
    private static final String DEV_EMAIL = "dev@ascend.app";

    private final UserRepository userRepository;

    public DevAuthFilter(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            ensureDevUserExists();

            FirebasePrincipal principal = new FirebasePrincipal(DEV_UID, DEV_EMAIL, "dev", Map.of());
            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                    principal, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        filterChain.doFilter(request, response);
    }

    private void ensureDevUserExists() {
        if (userRepository.findByFirebaseUid(DEV_UID).isEmpty()) {
            User devUser = User.builder()
                    .firebaseUid(DEV_UID)
                    .email(DEV_EMAIL)
                    .username("DevHero")
                    .level(1)
                    .xp(0L)
                    .league("BRONZE")
                    .role(UserRole.USER)
                    .premium(false)
                    .hardMode(false)
                    .guest(false)
                    .timezone("Asia/Kolkata")
                    .build();
            userRepository.save(devUser);
            log.info("Created dev user: {}", DEV_EMAIL);
        }
    }
}
