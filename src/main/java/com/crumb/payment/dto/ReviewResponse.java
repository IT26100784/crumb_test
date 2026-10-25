package com.crumb.payment.dto;

import com.crumb.payment.entity.Review;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * ReviewResponse - Output presentation for customer reviews and ratings.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponse {

    // ENCAPSULATION: private fields with getters/setters
    private Long id;
    private Long customerId;
    private String customerName;
    private Long productId;
    private String productName;
    private Long orderId;
    private int rating;
    private String comment;
    private boolean approved;
    private LocalDateTime createdAt;

    public static ReviewResponse fromEntity(Review review) {
        if (review == null) return null;
        return ReviewResponse.builder()
                .id(review.getId())
                .customerId(review.getCustomer() != null ? review.getCustomer().getId() : null)
                .customerName(review.getCustomerName())
                .productId(review.getProduct() != null ? review.getProduct().getId() : null)
                .productName(review.getProductName())
                .orderId(review.getOrder() != null ? review.getOrder().getId() : null)
                .rating(review.getRating())
                .comment(review.getComment())
                .approved(review.isApproved())
                .createdAt(review.getCreatedAt())
                .build();
    }
}
