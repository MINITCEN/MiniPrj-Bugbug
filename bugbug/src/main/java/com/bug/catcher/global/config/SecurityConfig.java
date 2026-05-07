package com.bug.catcher.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                // [추가된 부분] H2 콘솔의 iframe(화면 쪼개기)이 정상적으로 보이도록 허용합니다.
                .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.disable()))
                .authorizeHttpRequests(auth -> auth
                        // [수정된 부분] "/h2-console/**" 경로를 추가해서 누구나 접근할 수 있게 열어줍니다!
                        .requestMatchers("/api/users/signup", "/api/auth/login", "/h2-console/**","/api/auth/logout").permitAll()
                        .anyRequest().authenticated()
                );

        return http.build();
    }
}