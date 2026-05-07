package com.bug.catcher.global.config;

import com.bug.catcher.global.auth.LoginCheckInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LoginCheckInterceptor()) // 우리가 만든 스캐너를 등록합니다.
                .addPathPatterns("/**") // 1. 일단 모든 API 경로("/**")에 스캐너를 설치해서 다 막습니다.
                .excludePathPatterns(   // 2. 하지만 아래 주소들은 스캐너 검사에서 빼줍니다. (프리패스 구역)
                        "/api/users/signup", // 회원가입
                        "/api/auth/login",   // 로그인
                        "/api/auth/logout",  // 로그아웃
                        "/h2-console/**",    // DB 확인용
                        "/error"             // 기본 에러 페이지
                );
    }
}