package org.example.cookingappbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.beans.factory.annotation.Value;

import org.example.cookingappbackend.seed.SeedLoader;

@SpringBootApplication
public class CookingAppBackendApplication {

    @Bean
    CommandLineRunner seedRunner(SeedLoader loader,
                                 @Value("${app.seed.enabled:true}") boolean enabled) {
        return args -> { if (enabled) loader.runOnce(); };
    }

    public static void main(String[] args) {
        SpringApplication.run(CookingAppBackendApplication.class, args);
    }
}
