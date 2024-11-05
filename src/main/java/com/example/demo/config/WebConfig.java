package com.example.demo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**") // Allow all requests to the API
                .allowedOrigins("*")
                // .allowedOrigins("https://delhimetro-4lodvnnx8-mahaks-projects-ad3c59a9.vercel.app") // Allow requests from React app without trailing slash
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowCredentials(true); // If you need to allow credentials
    }
}