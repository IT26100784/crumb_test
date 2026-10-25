package com.crumb.payment.repository;

import com.crumb.payment.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * ReviewRepository - Data access interface for Review entities.
 * 
 * WHY: Provides query methods for product reviews, customer submissions,
 * approved public testimonials, and rating averages.
 */
@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    /**
     * Finds approved reviews for a specific product.
     */
    List<Review> findByProductIdAndApprovedTrueOrderByCreatedAtDesc(Long productId);

    /**
     * Finds all approved public reviews for the home page testimonials.
     */
    List<Review> findByApprovedTrueOrderByCreatedAtDesc();

    /**
     * Finds reviews submitted by a specific customer.
     */
    List<Review> findByCustomerIdOrderByCreatedAtDesc(Long customerId);

    /**
     * Retrieves all reviews for admin moderation.
     */
    List<Review> findAllByOrderByCreatedAtDesc();

    /**
     * Computes average rating for a given product.
     */
    @Query("SELECT COALESCE(AVG(r.rating), 5.0) FROM Review r WHERE r.product.id = :productId AND r.approved = true")
    double calculateAverageProductRating(Long productId);
}
