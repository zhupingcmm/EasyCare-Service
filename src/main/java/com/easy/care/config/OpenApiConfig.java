package com.easy.care.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("HR Maternity Calculator API")
                .version("1.0.0")
                .description("产假天数与生育津贴计算接口文档"))
            .servers(List.of(new Server().url("/").description("Default Server")));
    }
}
