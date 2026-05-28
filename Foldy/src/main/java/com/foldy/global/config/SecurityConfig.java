package com.foldy.global.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import jakarta.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtService jwtService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(exception -> exception
                    .authenticationEntryPoint((request, response, authException) -> {
                        // 인증 실패 시 뷰 페이지로 리다이렉트되지 않도록 401 에러와 명확한 JSON 메시지를 즉시 반환
                        response.setContentType("application/json;charset=UTF-8");
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401
                        response.getWriter().write("{\"result\":false,\"message\":\"인증에 실패했거나 토큰이 만료되었습니다.\",\"data\":null}");
                    })
                )
            .authorizeHttpRequests(auth -> auth
                // 정적 리소스
                .requestMatchers(
                    "/css/**",
                    "/js/**",
                    "/images/**",
                    "/plugins/**",
                    "/favicon.ico"
                ).permitAll()
                // 페이지 — 로그인/회원가입만 허용
                .requestMatchers(
                    "/",
                    "/auth/**",
                    "/memo/**"
                ).permitAll()
                // API — 로그인/회원가입만 허용
                .requestMatchers("/api/auth/signup", "/api/auth/login", "/api/auth/logout").permitAll()
                // actuator health (K8s probe용)
                .requestMatchers("/actuator/health").permitAll()
                // SecurityConfig.java
                .requestMatchers("/actuator/**").permitAll()
                // 나머지 전부 인증 필요
                .anyRequest().authenticated()
            )
            .addFilterBefore(
                new JwtFilter(jwtService),
                UsernamePasswordAuthenticationFilter.class
            );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}