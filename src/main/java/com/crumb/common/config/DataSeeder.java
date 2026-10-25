package com.crumb.common.config;

import com.crumb.customer.service.CustomerService;
import com.crumb.payment.service.ReviewService;
import com.crumb.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * DataSeeder - Application startup runner.
 * 
 * WHY: Initializes the default administrator account (admin@crumb.com / Admin@123),
 * populates authentic bakery products (cakes, pastries, sourdoughs) with Unsplash images,
 * and sets up customer reviews on first launch.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final CustomerService customerService;
    private final ProductService productService;
    private final ReviewService reviewService;

    @Override
    public void run(String... args) {
        log.info(">>> Crumb Bakery Platform: Initializing database seeds...");
        customerService.initDefaultAdmin();
        productService.initSampleProducts();
        reviewService.initSampleReviews();
        log.info(">>> Crumb Bakery Platform: Database initialization complete!");
    }
}
