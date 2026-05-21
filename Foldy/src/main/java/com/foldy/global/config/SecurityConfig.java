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
            .authorizeHttpRequests(auth -> auth
                // 정적 리소스
                .requestMatchers(
                    "/css/**",
                    "/js/**",
                    "/images/**",
                    "/plugins/**",
                    "/favicon.ico"
                ).permitAll()
                // 퍼블리싱 (개발용)
                .requestMatchers("/publishing/**").permitAll()
                // 페이지 — 로그인/회원가입만 허용
                .requestMatchers(
                    "/",
                    "/auth/**"
                ).permitAll()
                // API — 로그인/회원가입만 허용
                .requestMatchers("/api/auth/**").permitAll()
                // actuator health (K8s probe용)
                .requestMatchers("/actuator/health").permitAll()
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