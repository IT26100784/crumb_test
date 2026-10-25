package com.crumb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * CrumbApplication - Main Entry Point for the Crumb Bakery Platform.
 * 
 * WHY: Bootstraps the Spring Boot application context, scans for all beans,
 * entities, repositories, and REST controllers across all component packages.
 */
@SpringBootApplication
public class CrumbApplication {

    public static void main(String[] args) {
        SpringApplication.run(CrumbApplication.class, args);
    }
}
