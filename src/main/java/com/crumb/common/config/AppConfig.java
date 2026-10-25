package com.crumb.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * AppConfig - Shared application configuration.
 * 
 * WHY: Provides singleton Spring beans such as PasswordEncoder for secure
 * BCrypt password hashing across customer authentication and admin creation.
 */
@Configuration
public class AppConfig {

    /**
     * PasswordEncoder bean using BCrypt hashing.
     * WHY: Ensures customer passwords are never stored in plain text.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
