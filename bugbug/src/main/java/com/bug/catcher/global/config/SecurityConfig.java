package com.bug.catcher.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                // [추가된 부분] 시큐리티가 제공하는 기본 로그인 화면이 뜨지 않도록 막아줍니다. (우리만의 로그인 API를 쓰기 위함)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                // [추가된 부분] H2 콘솔의 iframe(화면 쪼개기)이 정상적으로 보이도록 허용합니다.
                .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.disable()))
                .authorizeHttpRequests(auth -> auth
                        // 권한 검사는 우리가 만든 LoginCheckInterceptor가 담당하므로,
                        // 시큐리티 필터에서는 일단 모두 통과.
                        // 이렇게 해야 인터셉터와 충돌이 발생하지 않음
                        .anyRequest().permitAll()
                );
        return http.build();
    }
}