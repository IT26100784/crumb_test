package com.crumb.payment.service;

import com.crumb.common.exception.ForbiddenException;
import com.crumb.common.exception.ResourceNotFoundException;
import com.crumb.customer.entity.Customer;
import com.crumb.customer.service.CustomerService;
import com.crumb.order.entity.Order;
import com.crumb.order.repository.OrderRepository;
import com.crumb.payment.dto.ReviewRequest;
import com.crumb.payment.dto.ReviewResponse;
import com.crumb.payment.entity.Review;
import com.crumb.payment.repository.ReviewRepository;
import com.crumb.product.entity.BakeryProduct;
import com.crumb.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ReviewServiceImpl - Implementation of ReviewService.
 * 
 * WHY: Manages customer feedback, rating aggregation, moderation,
 * and seeds authentic initial customer reviews.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final CustomerService customerService;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    /**
     * Submits a customer review and recalculates the product's average rating.
     */
    @Override
    public ReviewResponse submitReview(Long customerId, ReviewRequest request) {
        Customer customer = customerService.getCustomerEntity(customerId);
        BakeryProduct product = null;
        if (request.getProductId() != null) {
            product = productRepository.findById(request.getProductId()).orElse(null);
        }

        Order order = null;
        if (request.getOrderId() != null) {
            order = orderRepository.findById(request.getOrderId()).orElse(null);
        }

        Review review = new Review();
        review.setCustomer(customer);
        review.setCustomerName(customer.getFullName());
        review.setProduct(product);
        if (product != null) {
            review.setProductName(product.getName());
        }
        review.setOrder(order);
        review.setRating(request.getRating());
        review.setComment(request.getComment().trim());
        review.setApproved(true);

        Review saved = reviewRepository.save(review);

        // Update product rating and review count
        if (product != null) {
            double avg = reviewRepository.calculateAverageProductRating(product.getId());
            product.setRating(Math.round(avg * 10.0) / 10.0);
            product.setReviewCount(product.getReviewCount() + 1);
            productRepository.save(product);
        }

        return ReviewResponse.fromEntity(saved);
    }

    /**
     * Retrieves approved reviews for a product.
     */
    @Override
    @Transactional(readOnly = true)
    public List<ReviewResponse> getProductReviews(Long productId) {
        return reviewRepository.findByProductIdAndApprovedTrueOrderByCreatedAtDesc(productId)
                .stream()
                .map(ReviewResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves approved public reviews for testimonials showcase.
     */
    @Override
    @Transactional(readOnly = true)
    public List<ReviewResponse> getPublicReviews() {
        return reviewRepository.findByApprovedTrueOrderByCreatedAtDesc()
                .stream()
                .map(ReviewResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves customer reviews.
     */
    @Override
    @Transactional(readOnly = true)
    public List<ReviewResponse> getCustomerReviews(Long customerId) {
        return reviewRepository.findByCustomerIdOrderByCreatedAtDesc(customerId)
                .stream()
                .map(ReviewResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Admin endpoint: list all reviews.
     */
    @Override
    @Transactional(readOnly = true)
    public List<ReviewResponse> getAllReviews() {
        return reviewRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(ReviewResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Admin toggle to approve or hide reviews.
     */
    @Override
    public ReviewResponse toggleReviewApproval(Long id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + id));
        review.setApproved(!review.isApproved());
        Review saved = reviewRepository.save(review);
        return ReviewResponse.fromEntity(saved);
    }

    /**
     * Deletes review (owner or admin).
     */
    @Override
    public void deleteReview(Long id, Long customerId, boolean isAdmin) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + id));

        if (!isAdmin && !review.getCustomer().getId().equals(customerId)) {
            throw new ForbiddenException("You cannot delete another customer's review.");
        }

        reviewRepository.delete(review);
    }

    /**
     * Seeds initial sample reviews on first run.
     */
    @Override
    public void initSampleReviews() {
        if (reviewRepository.count() > 0) {
            return;
        }

        // Find admin customer to attach seed reviews or create seed reviews
        Customer customer = customerService.getCustomerEntity(1L);
        List<BakeryProduct> products = productRepository.findAll();

        if (customer != null && !products.isEmpty()) {
            List<Review> seedList = new ArrayList<>();

            BakeryProduct cake = products.stream()
                    .filter(p -> p.getName().contains("Chocolate"))
                    .findFirst().orElse(products.get(0));

            Review r1 = new Review(customer, cake, 5,
                    "Absolutely the most decadent chocolate cake in Colombo! The Belgian chocolate ganache is rich, smooth, and not overly sweet. Arrived in pristine condition.");
            r1.setCustomerName("Samantha Perera");
            seedList.add(r1);

            BakeryProduct croissant = products.stream()
                    .filter(p -> p.getName().contains("Croissant"))
                    .findFirst().orElse(products.get(1));

            Review r2 = new Review(customer, croissant, 5,
                    "Flaky, buttery, and authentically French! Heated it up for 2 minutes and it felt like sitting in a Parisian cafe. Will be ordering daily breakfast from Crumb!");
            r2.setCustomerName("Dr. Dinesh Jayawardena");
            seedList.add(r2);

            BakeryProduct bread = products.stream()
                    .filter(p -> p.getName().contains("Sourdough"))
                    .findFirst().orElse(products.get(2));

            Review r3 = new Review(customer, bread, 5,
                    "The open crumb and blistered crust on this wild sourdough are perfection. Excellent sour tang and keeps fresh in a bread bag for days.");
            r3.setCustomerName("Nadeesha Fernando");
            seedList.add(r3);

            Review r4 = new Review(customer, null, 5,
                    "Ordered a bespoke 2-tier birthday cake using the online 3D cake builder. The live preview pricing was accurate, and the final design surpassed our expectations!");
            r4.setCustomerName("Arjun Wijesinghe");
            seedList.add(r4);

            reviewRepository.saveAll(seedList);
            System.out.println(">>> SEED: Initialized 4 realistic customer reviews and testimonials.");
        }
    }
}
