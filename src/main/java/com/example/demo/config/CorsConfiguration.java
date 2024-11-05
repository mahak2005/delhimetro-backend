package com.example.demo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfiguration {

    @Value("${cors.allowedMethods:GET,POST,PUT,DELETE,OPTIONS}")
    private String allowedMethods;

    @Value("${cors.allowedHeaders:*}")
    private String allowedHeaders;

    @Value("${cors.allowedOriginPatterns:https://delhimetro.vercel.app}")
    private String allowedOriginPatterns;

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**") // Adjust this path as needed for your API
                        .allowedOrigins(allowedOriginPatterns)
                        .allowedMethods(allowedMethods.split(","))
                        .allowedHeaders(allowedHeaders.split(","))
                        .allowCredentials(true); // Enable credentials if needed
            }
        };
    }
}
