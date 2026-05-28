package com.ascend.auth.config;

import com.ascend.auth.service.FirebaseTokenService;
import com.ascend.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;
import java.util.stream.Stream;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    /** Exact-match public paths (no sub-path access needed). */
    private static final List<String> PUBLIC_PATHS_EXACT = List.of(
            "/api/v1/auth/login",
            "/api/v1/auth/register",
            "/actuator/health"
    );

    /** Prefix-match public paths (sub-paths are also public). */
    private static final List<String> PUBLIC_PATHS_PREFIX = List.of(
            "/swagger-ui",
            "/api-docs",
            "/v3/api-docs"
    );

    @Value("${app.cors.allowed-origins}")
    private List<String> allowedOrigins;

    @Bean
    public FirebaseTokenFilter firebaseTokenFilter(FirebaseTokenService firebaseTokenService,
                                                   UserRepository userRepository) {
        List<String> allPublicPaths = Stream.concat(
                PUBLIC_PATHS_EXACT.stream(),
                PUBLIC_PATHS_PREFIX.stream()
        ).toList();
        return new FirebaseTokenFilter(firebaseTokenService, userRepository, allPublicPaths);
    }

    @Bean
    public RateLimitFilter rateLimitFilter(StringRedisTemplate redisTemplate,
                                           RateLimitConfig rateLimitConfig) {
        return new RateLimitFilter(redisTemplate, rateLimitConfig);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   FirebaseTokenFilter firebaseTokenFilter,
                                                   RateLimitFilter rateLimitFilter) throws Exception {
        // Build request matchers: exact paths as-is, prefix paths with /** wildcard
        String[] matchers = Stream.concat(
                PUBLIC_PATHS_EXACT.stream(),
                PUBLIC_PATHS_PREFIX.stream().map(path -> path + "/**")
        ).toArray(String[]::new);

        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(matchers).permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(firebaseTokenFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(allowedOrigins);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
