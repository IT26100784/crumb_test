package com.crumb.payment.service;

import com.crumb.payment.dto.ReviewRequest;
import com.crumb.payment.dto.ReviewResponse;

import java.util.List;

/**
 * ReviewService - Business contract for customer testimonials and ratings.
 * 
 * // ABSTRACTION:
 * Defines business operations for rating submissions, catalog reviews, and moderation.
 */
// ABSTRACTION
public interface ReviewService {

    /**
     * Submits a customer review for a bakery product or overall experience.
     */
    ReviewResponse submitReview(Long customerId, ReviewRequest request);

    /**
     * Retrieves approved reviews for a specific product.
     */
    List<ReviewResponse> getProductReviews(Long productId);

    /**
     * Retrieves top public approved reviews for the home page showcase.
     */
    List<ReviewResponse> getPublicReviews();

    /**
     * Retrieves reviews submitted by a specific customer.
     */
    List<ReviewResponse> getCustomerReviews(Long customerId);

    /**
     * Admin endpoint: Lists all reviews for moderation.
     */
    List<ReviewResponse> getAllReviews();

    /**
     * Admin toggle to approve or hide a review.
     */
    ReviewResponse toggleReviewApproval(Long id);

    /**
     * Deletes a review (owner or admin).
     */
    void deleteReview(Long id, Long customerId, boolean isAdmin);

    /**
     * Seeds initial realistic customer reviews on first run.
     */
    void initSampleReviews();
}
