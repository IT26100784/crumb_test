package com.crumb.payment.repository;

import com.crumb.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * PaymentRepository - Data access interface for Payment records.
 * 
 * WHY: Provides query methods for transaction lookups, customer payment history,
 * and admin financial audits.
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    /**
     * Finds payments made by a customer.
     */
    List<Payment> findByCustomerIdOrderByPaymentDateDesc(Long customerId);

    /**
     * Finds payment by unique transaction reference code.
     */
    Optional<Payment> findByPaymentReference(String paymentReference);

    /**
     * Finds payments by status (PAID, REFUNDED, FAILED).
     */
    List<Payment> findByStatusOrderByPaymentDateDesc(String status);

    /**
     * Retrieves all payments ordered newest first.
     */
    List<Payment> findAllByOrderByPaymentDateDesc();

    /**
     * Computes total processed payments volume.
     */
    @Query("SELECT COALESCE(SUM(p.amount), 0.0) FROM Payment p WHERE p.status = 'PAID'")
    double calculateTotalCollected();
}
