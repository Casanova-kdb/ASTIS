package com.astis.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI astisOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("ASTIS Backend API")
                        .version("0.1.0")
                        .description("REST APIs for authentication, task management, analytics, and AI recommendations."));
    }
}
