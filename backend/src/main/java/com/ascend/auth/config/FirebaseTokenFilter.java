package com.ascend.auth.config;

import com.ascend.auth.entity.UserRole;
import com.ascend.auth.service.FirebaseAuthenticationException;
import com.ascend.auth.service.FirebaseTokenService;
import com.ascend.user.entity.User;
import com.ascend.user.repository.UserRepository;
import com.google.firebase.auth.FirebaseToken;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Filter that intercepts incoming requests, extracts the Firebase ID token
 * from the Authorization header, verifies it, and sets the Spring Security context
 * with the user's role-based authorities loaded from the database.
 */
@Slf4j
public class FirebaseTokenFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final int MIN_TOKEN_LENGTH = 100;
    private static final int MAX_TOKEN_LENGTH = 5000;
    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    private final FirebaseTokenService firebaseTokenService;
    private final UserRepository userRepository;
    private final List<String> publicPaths;

    public FirebaseTokenFilter(FirebaseTokenService firebaseTokenService,
                               UserRepository userRepository,
                               List<String> publicPaths) {
        this.firebaseTokenService = firebaseTokenService;
        this.userRepository = userRepository;
        this.publicPaths = publicPaths;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String token = extractBearerToken(request);

        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }

        if (token.length() < MIN_TOKEN_LENGTH || token.length() > MAX_TOKEN_LENGTH) {
            log.debug("Token length {} outside expected range [{}, {}], skipping verification",
                    token.length(), MIN_TOKEN_LENGTH, MAX_TOKEN_LENGTH);
            filterChain.doFilter(request, response);
            return;
        }

        try {
            FirebaseToken decodedToken = firebaseTokenService.verifyToken(token);
            String uid = decodedToken.getUid();
            String email = decodedToken.getEmail();
            String provider = firebaseTokenService.getProvider(decodedToken);

            FirebasePrincipal principal = new FirebasePrincipal(uid, email, provider, decodedToken.getClaims());

            // Load user role from database and build authorities with role hierarchy
            List<SimpleGrantedAuthority> authorities = loadAuthorities(uid);

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(principal, null, authorities);
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            log.debug("Authenticated user uid={} provider={} authorities={}", uid, provider, authorities);

        } catch (FirebaseAuthenticationException e) {
            log.warn("Token verification failed: {}", e.getMessage());
            SecurityContextHolder.clearContext();
        } catch (Exception e) {
            log.warn("Unexpected error during token verification", e);
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return publicPaths.stream().anyMatch(publicPath ->
                PATH_MATCHER.match(publicPath, path) || path.startsWith(publicPath));
    }

    /**
     * Loads the user's role from the database and builds a list of granted authorities
     * with role hierarchy (higher roles inherit lower role permissions).
     */
    private List<SimpleGrantedAuthority> loadAuthorities(String firebaseUid) {
        Optional<User> userOpt = userRepository.findByFirebaseUid(firebaseUid);

        UserRole role = userOpt
                .map(User::getRole)
                .orElse(UserRole.USER);

        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + role.name()));

        // Add inherited role authorities (higher roles include lower role permissions)
        switch (role) {
            case SUPER_ADMIN:
                authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
                // fall through
            case ADMIN:
                authorities.add(new SimpleGrantedAuthority("ROLE_MODERATOR"));
                // fall through
            case MODERATOR:
                authorities.add(new SimpleGrantedAuthority("ROLE_PREMIUM_USER"));
                // fall through
            case PREMIUM_USER:
                authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
                break;
            case USER:
            default:
                break;
        }

        return authorities;
    }

    private String extractBearerToken(HttpServletRequest request) {
        String header = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(header)
                && header.regionMatches(true, 0, BEARER_PREFIX, 0, BEARER_PREFIX.length())) {
            return header.substring(BEARER_PREFIX.length());
        }
        return null;
    }
}
