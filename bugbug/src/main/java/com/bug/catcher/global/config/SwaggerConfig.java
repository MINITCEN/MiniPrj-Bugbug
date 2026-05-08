package com.bug.catcher.global.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("벌레 잡기 매칭 서비스 API")
                        .description("Bugbug 서비스의 API 명세서입니다.")
                        .version("v1.0.0"));
    }
}
