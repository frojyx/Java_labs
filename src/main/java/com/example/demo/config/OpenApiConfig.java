package com.example.demo.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI restaurantPlatformOpenApi() {
        return new OpenAPI()
            .info(new Info()
                .title("Restaurant Platform API")
                .description("API для управления блюдами, заказами, клиентами, категориями и ингредиентами")
                .version("v1")
                .contact(new Contact().name("Demo Project"))
                .license(new License().name("Apache 2.0")));
    }
}
