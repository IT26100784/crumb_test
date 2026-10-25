package com.crumb.payment.controller;

import com.crumb.common.auth.SessionAuthHelper;
import com.crumb.common.dto.ApiResponse;
import com.crumb.payment.dto.ReviewRequest;
import com.crumb.payment.dto.ReviewResponse;
import com.crumb.payment.service.ReviewService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ReviewController - REST API for ratings, customer reviews, and testimonials.
 * 
 * WHY: Provides public feedback displays and customer rating submission endpoints.
 */
@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    /**
     * Customer endpoint: Submit feedback and star rating for a product or bakery order.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ReviewResponse>> submitReview(
            @Valid @RequestBody ReviewRequest request,
            HttpSession session) {
        Long customerId = SessionAuthHelper.requireLoggedIn(session);
        ReviewResponse response = reviewService.submitReview(customerId, request);
        return ResponseEntity.ok(ApiResponse.success("Thank you for your review!", response));
    }

    /**
     * Public endpoint: Retrieve approved reviews for a specific product.
     */
    @GetMapping("/product/{productId}")
    public ResponseEntity<ApiResponse<List<ReviewResponse>>> getProductReviews(@PathVariable Long productId) {
        List<ReviewResponse> list = reviewService.getProductReviews(productId);
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    /**
     * Public endpoint: Retrieve testimonials showcase for homepage.
     */
    @GetMapping("/testimonials")
    public ResponseEntity<ApiResponse<List<ReviewResponse>>> getTestimonials() {
        List<ReviewResponse> list = reviewService.getPublicReviews();
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    /**
     * Customer endpoint: View personal reviews submitted.
     */
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<ReviewResponse>>> getMyReviews(HttpSession session) {
        Long customerId = SessionAuthHelper.requireLoggedIn(session);
        List<ReviewResponse> list = reviewService.getCustomerReviews(customerId);
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    /**
     * Customer or admin delete endpoint.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteReview(
            @PathVariable Long id,
            HttpSession session) {
        Long currentUserId = SessionAuthHelper.requireLoggedIn(session);
        boolean isAdmin = SessionAuthHelper.isAdmin(session);

        reviewService.deleteReview(id, currentUserId, isAdmin);
        return ResponseEntity.ok(ApiResponse.success("Review deleted.", null));
    }

    /**
     * Admin endpoint: Retrieve all reviews for moderation.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<ReviewResponse>>> getAllReviews(HttpSession session) {
        SessionAuthHelper.requireAdmin(session);
        List<ReviewResponse> list = reviewService.getAllReviews();
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    /**
     * Admin endpoint: Toggle review approval status.
     */
    @PutMapping("/{id}/toggle")
    public ResponseEntity<ApiResponse<ReviewResponse>> toggleReviewApproval(
            @PathVariable Long id,
            HttpSession session) {
        SessionAuthHelper.requireAdmin(session);
        ReviewResponse updated = reviewService.toggleReviewApproval(id);
        return ResponseEntity.ok(ApiResponse.success(
                updated.isApproved() ? "Review approved and visible." : "Review hidden from storefront.",
                updated
        ));
    }
}
