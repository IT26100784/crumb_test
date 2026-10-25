package com.crumb.customer.controller;

import com.crumb.common.auth.SessionAuthHelper;
import com.crumb.common.dto.ApiResponse;
import com.crumb.customer.dto.CustomerResponse;
import com.crumb.customer.dto.LoginRequest;
import com.crumb.customer.dto.RegisterRequest;
import com.crumb.customer.service.CustomerService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * CustomerAuthController - Endpoints for account registration, login, logout, and session check.
 * 
 * WHY: Provides public authentication APIs consumed by the frontend login/signup modal.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class CustomerAuthController {

    private final CustomerService customerService;

    /**
     * Registers a new customer and automatically logs them into the HTTP session.
     * WHY: Streamlines onboarding so users can order immediately after signing up.
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<CustomerResponse>> register(
            @Valid @RequestBody RegisterRequest request,
            HttpSession session) {
        CustomerResponse customer = customerService.register(request);
        SessionAuthHelper.loginUser(session, customer.getId(), customer.getFullName(), customer.getEmail(), customer.getRole());
        return ResponseEntity.ok(ApiResponse.success("Account registered successfully! Welcome to Crumb.", customer));
    }

    /**
     * Authenticates customer credentials and establishes an HTTP session.
     * WHY: Securely identifies users for personalized orders, bookings, and VIP perks.
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<CustomerResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpSession session) {
        CustomerResponse customer = customerService.authenticate(request);
        SessionAuthHelper.loginUser(session, customer.getId(), customer.getFullName(), customer.getEmail(), customer.getRole());
        return ResponseEntity.ok(ApiResponse.success("Welcome back, " + customer.getFullName() + "!", customer));
    }

    /**
     * Destroys current HTTP session on logout.
     * WHY: Cleans up authentication state securely.
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(HttpSession session) {
        SessionAuthHelper.logoutUser(session);
        return ResponseEntity.ok(ApiResponse.success("Logged out successfully.", null));
    }

    /**
     * Checks current session to determine if a visitor is currently logged in.
     * WHY: Enables frontend pages to auto-restore active session, user name, and role.
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<CustomerResponse>> getCurrentUser(HttpSession session) {
        Long userId = SessionAuthHelper.getCurrentUserId(session);
        if (userId == null) {
            return ResponseEntity.ok(ApiResponse.success("Guest session", null));
        }
        CustomerResponse customer = customerService.getCustomerById(userId);
        return ResponseEntity.ok(ApiResponse.success("Active session", customer));
    }
}
