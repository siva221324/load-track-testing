package com.loadtrack.config;

import com.loadtrack.security.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final CorsConfigurationSource corsConfigurationSource;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource))
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/error").permitAll()
                .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**", "/v3/api-docs").permitAll()
                .requestMatchers("/api/trucks/**").hasRole("ADMIN")
                .requestMatchers("/api/drivers/**").hasRole("ADMIN")
                .requestMatchers("/api/dealers/**").hasRole("ADMIN")
                // Sand types: anyone authenticated can READ (dealers need this to submit requests),
                // but only admin can create/update/delete
                .requestMatchers(HttpMethod.GET, "/api/sand-types/**").hasAnyRole("ADMIN", "DEALER", "DRIVER")
                .requestMatchers("/api/sand-types/**").hasRole("ADMIN")
                .requestMatchers("/api/settings/**").hasRole("ADMIN")
                .requestMatchers("/api/trips/**").hasRole("ADMIN")
                .requestMatchers("/api/payments/**").hasRole("ADMIN")
                .requestMatchers("/api/receipts/**").hasAnyRole("ADMIN", "DEALER")
                .requestMatchers("/api/dashboard/**").hasRole("ADMIN")
                .requestMatchers("/api/reports/**").hasRole("ADMIN")
                .requestMatchers("/api/trip-requests/**").hasRole("ADMIN")
                .requestMatchers("/api/me/dealer/trip-requests/**").hasRole("DEALER")
                .requestMatchers("/api/me/driver/**").hasRole("DRIVER")
                .requestMatchers("/api/me/dealer/**").hasRole("DEALER")
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
