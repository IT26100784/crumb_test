package com.crumb.payment.service;

import com.crumb.common.exception.BadRequestException;
import com.crumb.common.exception.ResourceNotFoundException;
import com.crumb.customcake.entity.CustomCake;
import com.crumb.customcake.repository.CustomCakeRepository;
import com.crumb.customer.entity.Customer;
import com.crumb.customer.service.CustomerService;
import com.crumb.order.entity.Order;
import com.crumb.order.repository.OrderRepository;
import com.crumb.payment.dto.PaymentRequest;
import com.crumb.payment.dto.PaymentResponse;
import com.crumb.payment.entity.CardPayment;
import com.crumb.payment.entity.CashPayment;
import com.crumb.payment.entity.Payment;
import com.crumb.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * PaymentServiceImpl - Implementation of PaymentService.
 * 
 * WHY: Orchestrates monetary transactions, executes polymorphic payment logic,
 * updates linked order and custom cake statuses, and calculates financial metrics.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final CustomerService customerService;
    private final OrderRepository orderRepository;
    private final CustomCakeRepository customCakeRepository;

    /**
     * Executes payment processing.
     * Demonstrates POLYMORPHISM by calling processPayment on the abstract Payment reference.
     */
    @Override
    public PaymentResponse processPayment(Long customerId, PaymentRequest request) {
        Customer customer = customerService.getCustomerEntity(customerId);

        Payment payment;
        String type = request.getPaymentType() != null ? request.getPaymentType().toUpperCase() : "CARD";

        if ("CASH".equalsIgnoreCase(type)) {
            CashPayment cash = new CashPayment();
            double tendered = request.getCashTendered() != null ? request.getCashTendered() : request.getAmount();
            cash.setCashTendered(tendered);
            cash.setCollectorStaffName(request.getCollectorStaffName() != null ? request.getCollectorStaffName().trim() : "Crumb Bakery Cashier");
            payment = cash;
        } else {
            CardPayment card = new CardPayment();
            card.setCardType(request.getCardType() != null ? request.getCardType().trim() : "Visa");
            String rawNum = request.getCardNumber() != null ? request.getCardNumber().replaceAll("\\s+", "") : "4242";
            card.setLastFourDigits(rawNum.length() >= 4 ? rawNum.substring(rawNum.length() - 4) : "4242");
            card.setCardHolderName(request.getCardHolderName() != null ? request.getCardHolderName().trim() : customer.getFullName());
            payment = card;
        }

        // Generate transaction reference code
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        int rand = ThreadLocalRandom.current().nextInt(1000, 9999);
        payment.setPaymentReference("CRUMB-PAY-" + datePart + "-" + rand);
        payment.setCustomer(customer);
        payment.setPaymentDate(LocalDateTime.now());

        // POLYMORPHISM: execute polymorphic tender logic
        boolean success = payment.processPayment(request.getAmount());
        if (!success) {
            throw new BadRequestException("Payment failed: Cash tendered ($" +
                    request.getCashTendered() + ") is less than the required amount ($" + request.getAmount() + ").");
        }

        // Link and advance Order if provided
        if (request.getOrderId() != null) {
            Order order = orderRepository.findById(request.getOrderId()).orElse(null);
            if (order != null) {
                payment.setOrder(order);
                if ("PENDING".equalsIgnoreCase(order.getStatus())) {
                    order.setStatus("CONFIRMED");
                }
            }
        }

        // Link and advance Custom Cake if provided
        if (request.getCustomCakeId() != null) {
            CustomCake cake = customCakeRepository.findById(request.getCustomCakeId()).orElse(null);
            if (cake != null) {
                payment.setCustomCake(cake);
                if ("PENDING_REVIEW".equalsIgnoreCase(cake.getStatus())) {
                    cake.setStatus("APPROVED");
                }
            }
        }

        Payment saved = paymentRepository.save(payment);
        return PaymentResponse.fromEntity(saved);
    }

    /**
     * Retrieves all payments for a customer.
     */
    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> getCustomerPayments(Long customerId) {
        return paymentRepository.findByCustomerIdOrderByPaymentDateDesc(customerId)
                .stream()
                .map(PaymentResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Admin query: list all payments with optional status and method filters.
     */
    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> getAllPayments(String status, String paymentType) {
        List<Payment> list;
        if (status != null && !status.trim().isEmpty() && !"ALL".equalsIgnoreCase(status)) {
            list = paymentRepository.findByStatusOrderByPaymentDateDesc(status.trim().toUpperCase());
        } else {
            list = paymentRepository.findAllByOrderByPaymentDateDesc();
        }

        return list.stream()
                .filter(p -> {
                    if (paymentType == null || paymentType.trim().isEmpty() || "ALL".equalsIgnoreCase(paymentType)) {
                        return true;
                    }
                    if ("CASH".equalsIgnoreCase(paymentType)) return p instanceof CashPayment;
                    if ("CARD".equalsIgnoreCase(paymentType)) return p instanceof CardPayment;
                    return true;
                })
                .map(PaymentResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves payment by ID.
     */
    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentById(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + id));
        return PaymentResponse.fromEntity(payment);
    }

    /**
     * Retrieves payment by reference code.
     */
    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentByReference(String reference) {
        Payment payment = paymentRepository.findByPaymentReference(reference)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with reference: " + reference));
        return PaymentResponse.fromEntity(payment);
    }

    /**
     * Admin refund processing.
     */
    @Override
    public PaymentResponse refundPayment(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + id));

        if ("REFUNDED".equalsIgnoreCase(payment.getStatus())) {
            throw new BadRequestException("Payment has already been refunded.");
        }

        payment.setStatus("REFUNDED");
        if (payment.getOrder() != null) {
            payment.getOrder().setStatus("CANCELLED");
        }
        if (payment.getCustomCake() != null) {
            payment.getCustomCake().setStatus("CANCELLED");
        }

        Payment saved = paymentRepository.save(payment);
        return PaymentResponse.fromEntity(saved);
    }

    /**
     * Total collected revenue across all settled transactions.
     */
    @Override
    @Transactional(readOnly = true)
    public double getTotalCollectedRevenue() {
        return Math.round(paymentRepository.calculateTotalCollected() * 100.0) / 100.0;
    }
}
