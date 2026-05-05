package com.example.demo;

import java.net.URI;
import java.net.URISyntaxException;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class DemoApplication {
    public static void main(String[] args) {
        applyRenderDatabaseUrl();
        SpringApplication.run(DemoApplication.class, args);
    }

    private static void applyRenderDatabaseUrl() {
        String existingUrl = System.getProperty("spring.datasource.url");
        if (existingUrl == null || existingUrl.isBlank()) {
            existingUrl = System.getenv("SPRING_DATASOURCE_URL");
        }

        if (existingUrl != null && !existingUrl.isBlank()) {
            return;
        }

        String databaseUrl = System.getenv("DATABASE_URL");
        if (databaseUrl == null || databaseUrl.isBlank()) {
            return;
        }

        try {
            URI uri = new URI(databaseUrl);
            String jdbcUrl = "jdbc:postgresql://" + uri.getHost() + ":" + uri.getPort()
                    + uri.getPath();

            System.setProperty("spring.datasource.url", jdbcUrl);

            String userInfo = uri.getUserInfo();
            if (userInfo != null && !userInfo.isBlank()) {
                String[] credentials = userInfo.split(":", 2);
                if ((System.getenv("SPRING_DATASOURCE_USERNAME") == null
                        || System.getenv("SPRING_DATASOURCE_USERNAME").isBlank())
                        && credentials.length > 0 && !credentials[0].isBlank()) {
                    System.setProperty("spring.datasource.username", credentials[0]);
                }
                if ((System.getenv("SPRING_DATASOURCE_PASSWORD") == null
                        || System.getenv("SPRING_DATASOURCE_PASSWORD").isBlank())
                        && credentials.length > 1 && !credentials[1].isBlank()) {
                    System.setProperty("spring.datasource.password", credentials[1]);
                }
            }
        } catch (URISyntaxException ignored) {
            // Leave Spring Boot defaults in place if Render provided an invalid URL.
        }
    }
}
