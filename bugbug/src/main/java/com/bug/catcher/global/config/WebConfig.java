package com.bug.catcher.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    // application.properties 에서 파일 저장 경로를 읽어옵니다. (없으면 기본값 사용)
    @Value("${file.upload.dir:C:/bugbug-uploads/}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 1. 프론트엔드에서 /uploads/** 로 시작하는 주소로 요청을 보내면
        // 2. 실제 내 컴퓨터의 uploadDir(C:/bugbug-uploads/) 경로에 있는 파일을 찾아서 응답해줍니다.
        // 참고: 윈도우 환경에서는 file:/// 를 붙여야 정확히 인식합니다.
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:///" + uploadDir);
    }
}
