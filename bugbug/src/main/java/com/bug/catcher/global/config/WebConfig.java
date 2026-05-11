package com.bug.catcher.global.config;

import com.bug.catcher.global.auth.LoginCheckInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    // 1. 인터셉터 설정 (로그인 체크)
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LoginCheckInterceptor())
                .addPathPatterns("/**") // 모든 경로 검사
                .excludePathPatterns(   // 예외 경로 (프리패스)
                        "/api/users/signup",
                        "/api/auth/login",
                        "/api/auth/logout",
                        "/api/request/wholeList",
                        "/h2-console/**",
                        "/error",
                        "/uploads/**",// 파일 접근은 로그인 체크에서 제외해야 이미지가 보입니다.
                        "/swagger-ui/**",// 스웨거 화면 접근 허용

                        "/v3/api-docs/**",   // 스웨거 데이터 접근 허용
                        "/swagger-resources/**",
                        "/api/v1/mosquito/**",
                        "/uploads/**",    // 파일 접근은 로그인 체크에서 제외해야 이미지가 보입니다.
                        "/swagger-ui/**",    // 스웨거 화면 접근 허용
                        "/v3/api-docs/**",   // 스웨거 데이터 접근 허용
                        "/swagger-resources/**",
                        "/api/v1/mosquito/**"
                );
    }

    // 2. 리소스 핸들러 설정 (업로드된 파일 서빙)
    @Override // @Override 어노테이션 추가
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path uploadDir = Paths.get("uploads");
        String uploadPath = uploadDir.toFile().getAbsolutePath();

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadPath + "/");
    }

    // 3. CORS 설정 (프론트엔드-백엔드 통신 허용)
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}