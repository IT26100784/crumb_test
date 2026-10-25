package com.crumb.customcake.service;

import com.crumb.common.exception.BadRequestException;
import com.crumb.common.exception.ForbiddenException;
import com.crumb.common.exception.ResourceNotFoundException;
import com.crumb.customcake.dto.CustomCakeRequest;
import com.crumb.customcake.dto.CustomCakeResponse;
import com.crumb.customcake.dto.CustomCakeStatusUpdate;
import com.crumb.customcake.entity.BirthdayCake;
import com.crumb.customcake.entity.CelebrationCake;
import com.crumb.customcake.entity.CustomCake;
import com.crumb.customcake.entity.WeddingCake;
import com.crumb.customcake.repository.CustomCakeRepository;
import com.crumb.customer.entity.Customer;
import com.crumb.customer.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * CustomCakeServiceImpl - Implementation of CustomCakeService.
 * 
 * WHY: Orchestrates custom cake orders, enforces kitchen preparation lead time rules,
 * invokes polymorphic price calculators, and tracks order statuses.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class CustomCakeServiceImpl implements CustomCakeService {

    private final CustomCakeRepository customCakeRepository;
    private final CustomerService customerService;

    /**
     * Creates and submits a new custom cake request.
     * Enforces lead time rules and computes polymorphic estimated price.
     */
    @Override
    public CustomCakeResponse createRequest(Long customerId, CustomCakeRequest request) {
        Customer customer = customerService.getCustomerEntity(customerId);
        CustomCake cake = instantiateSubclass(request);
        cake.setCustomer(customer);
        populateCommonAttributes(cake, request);

        // Lead time rule validation
        int minLeadDays = cake.getPreparationLeadDays();
        LocalDate earliestAllowed = LocalDate.now().plusDays(minLeadDays);
        if (request.getEventDate().isBefore(earliestAllowed)) {
            throw new BadRequestException("Our pastry chefs require at least " + minLeadDays +
                    " days advance notice for " + cake.getEventSummary() + ". Earliest date is " + earliestAllowed);
        }

        // POLYMORPHISM: compute price based on concrete subclass
        double price = cake.calculateEstimatedPrice();
        // Check if customer gets a tier discount (e.g. VIP 10% discount)
        double discount = customer.calculateDiscount(price);
        cake.setEstimatedPrice(Math.round((price - discount) * 100.0) / 100.0);
        cake.setStatus("PENDING_REVIEW");

        CustomCake saved = customCakeRepository.save(cake);
        return CustomCakeResponse.fromEntity(saved);
    }

    /**
     * Calculates instant price preview without saving to DB.
     * Demonstrates POLYMORPHISM by instantiating the subclass in memory.
     */
    @Override
    @Transactional(readOnly = true)
    public double calculatePreviewPrice(CustomCakeRequest request) {
        CustomCake cake = instantiateSubclass(request);
        populateCommonAttributes(cake, request);
        return cake.calculateEstimatedPrice();
    }

    /**
     * Retrieves all custom cake requests submitted by a customer.
     */
    @Override
    @Transactional(readOnly = true)
    public List<CustomCakeResponse> getCustomerRequests(Long customerId) {
        return customCakeRepository.findByCustomerIdOrderByCreatedAtDesc(customerId)
                .stream()
                .map(CustomCakeResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Admin endpoint: lists all custom cake requests.
     */
    @Override
    @Transactional(readOnly = true)
    public List<CustomCakeResponse> getAllRequests(String status) {
        List<CustomCake> list;
        if (status != null && !status.trim().isEmpty() && !"ALL".equalsIgnoreCase(status)) {
            list = customCakeRepository.findByStatusOrderByCreatedAtDesc(status.trim().toUpperCase());
        } else {
            list = customCakeRepository.findAllByOrderByCreatedAtDesc();
        }

        return list.stream()
                .map(CustomCakeResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves request by primary key.
     */
    @Override
    @Transactional(readOnly = true)
    public CustomCakeResponse getRequestById(Long id) {
        return CustomCakeResponse.fromEntity(getRequestEntity(id));
    }

    /**
     * Retrieves entity by ID.
     */
    @Override
    @Transactional(readOnly = true)
    public CustomCake getRequestEntity(Long id) {
        return customCakeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Custom cake request not found with id: " + id));
    }

    /**
     * Admin status transition and price revision.
     */
    @Override
    public CustomCakeResponse updateStatus(Long id, CustomCakeStatusUpdate update) {
        CustomCake cake = getRequestEntity(id);
        cake.setStatus(update.getStatus().trim().toUpperCase());
        if (update.getRevisedPrice() != null && update.getRevisedPrice() > 0) {
            cake.setEstimatedPrice(update.getRevisedPrice());
        }
        CustomCake saved = customCakeRepository.save(cake);
        return CustomCakeResponse.fromEntity(saved);
    }

    /**
     * Cancels a pending custom cake request.
     */
    @Override
    public CustomCakeResponse cancelRequest(Long id, Long customerId, boolean isAdmin) {
        CustomCake cake = getRequestEntity(id);
        if (!isAdmin && !cake.getCustomer().getId().equals(customerId)) {
            throw new ForbiddenException("You cannot cancel another customer's cake request.");
        }

        if ("BAKING".equalsIgnoreCase(cake.getStatus()) || "READY".equalsIgnoreCase(cake.getStatus()) || "COMPLETED".equalsIgnoreCase(cake.getStatus())) {
            throw new BadRequestException("Cannot cancel an order that is already baking or completed.");
        }

        cake.setStatus("CANCELLED");
        CustomCake saved = customCakeRepository.save(cake);
        return CustomCakeResponse.fromEntity(saved);
    }

    /**
     * Deletes a custom cake request (Admin only).
     */
    @Override
    public void deleteRequest(Long id) {
        CustomCake cake = getRequestEntity(id);
        customCakeRepository.delete(cake);
    }

    // Helper: Instantiates concrete subclass based on cakeType
    private CustomCake instantiateSubclass(CustomCakeRequest request) {
        String type = request.getCakeType() != null ? request.getCakeType().toUpperCase() : "BIRTHDAY";
        return switch (type) {
            case "WEDDING" -> {
                WeddingCake w = new WeddingCake();
                if (request.getTierCount() != null) w.setTierCount(request.getTierCount());
                if (request.getFreshFlorals() != null) w.setFreshFlorals(request.getFreshFlorals());
                if (request.getTastingKitOrdered() != null) w.setTastingKitOrdered(request.getTastingKitOrdered());
                yield w;
            }
            case "CELEBRATION" -> {
                CelebrationCake c = new CelebrationCake();
                if (request.getCelebrationType() != null) c.setCelebrationType(request.getCelebrationType());
                if (request.getEdibleGoldLeaf() != null) c.setEdibleGoldLeaf(request.getEdibleGoldLeaf());
                yield c;
            }
            default -> {
                BirthdayCake b = new BirthdayCake();
                if (request.getAgeNumber() != null) b.setAgeNumber(request.getAgeNumber());
                if (request.getCandleSetIncluded() != null) b.setCandleSetIncluded(request.getCandleSetIncluded());
                if (request.getCharacterTheme() != null) b.setCharacterTheme(request.getCharacterTheme());
                yield b;
            }
        };
    }

    // Helper: Populates common attributes
    private void populateCommonAttributes(CustomCake cake, CustomCakeRequest request) {
        cake.setFlavor(request.getFlavor().trim());
        cake.setSizeTiers(request.getSizeTiers().trim());
        cake.setFilling(request.getFilling() != null ? request.getFilling().trim() : "Vanilla Buttercream");
        cake.setFrosting(request.getFrosting() != null ? request.getFrosting().trim() : "Swiss Buttercream");
        cake.setInscription(request.getInscription() != null ? request.getInscription().trim() : "");
        cake.setColorTheme(request.getColorTheme() != null ? request.getColorTheme().trim() : "Pastel Elegance");
        cake.setDietaryNotes(request.getDietaryNotes() != null ? request.getDietaryNotes().trim() : "None");
        cake.setSpecialInstructions(request.getSpecialInstructions() != null ? request.getSpecialInstructions().trim() : "");
        cake.setReferenceImageUrl(request.getReferenceImageUrl() != null && !request.getReferenceImageUrl().trim().isEmpty()
                ? request.getReferenceImageUrl().trim()
                : "https://images.unsplash.com/photo-1535141192574-5d4897c13136?w=800");
        cake.setEventDate(request.getEventDate());
    }
}
