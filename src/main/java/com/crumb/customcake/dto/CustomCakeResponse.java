package com.crumb.customcake.dto;

import com.crumb.customcake.entity.BirthdayCake;
import com.crumb.customcake.entity.CelebrationCake;
import com.crumb.customcake.entity.CustomCake;
import com.crumb.customcake.entity.WeddingCake;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * CustomCakeResponse - Detailed output presentation model for custom cake orders.
 * 
 * WHY: Presents custom cake specifications, customer details, status timeline,
 * and polymorphic calculations (event summary, lead days, specialty addons).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomCakeResponse {

    // ENCAPSULATION: private fields with getters/setters
    private Long id;
    private Long customerId;
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private String flavor;
    private String sizeTiers;
    private String filling;
    private String frosting;
    private String inscription;
    private String colorTheme;
    private String dietaryNotes;
    private String specialInstructions;
    private String referenceImageUrl;
    private LocalDate eventDate;
    private String status;
    private double estimatedPrice;
    private String cakeType;
    private LocalDateTime createdAt;

    // POLYMORPHIC calculated fields
    private String eventSummary;
    private int preparationLeadDays;
    private String specialtyAddons;

    // Subclass fields
    private Integer ageNumber;
    private Boolean candleSetIncluded;
    private String characterTheme;
    private Integer tierCount;
    private Boolean freshFlorals;
    private Boolean tastingKitOrdered;
    private String celebrationType;
    private Boolean edibleGoldLeaf;

    /**
     * Converts a CustomCake entity into a CustomCakeResponse DTO.
     * Demonstrates POLYMORPHISM by calling overridden methods on CustomCake.
     */
    public static CustomCakeResponse fromEntity(CustomCake cake) {
        if (cake == null) return null;

        CustomCakeResponseBuilder builder = CustomCakeResponse.builder()
                .id(cake.getId())
                .customerId(cake.getCustomer() != null ? cake.getCustomer().getId() : null)
                .customerName(cake.getCustomer() != null ? cake.getCustomer().getFullName() : "N/A")
                .customerEmail(cake.getCustomer() != null ? cake.getCustomer().getEmail() : "N/A")
                .customerPhone(cake.getCustomer() != null ? cake.getCustomer().getPhone() : "N/A")
                .flavor(cake.getFlavor())
                .sizeTiers(cake.getSizeTiers())
                .filling(cake.getFilling())
                .frosting(cake.getFrosting())
                .inscription(cake.getInscription())
                .colorTheme(cake.getColorTheme())
                .dietaryNotes(cake.getDietaryNotes())
                .specialInstructions(cake.getSpecialInstructions())
                .referenceImageUrl(cake.getReferenceImageUrl())
                .eventDate(cake.getEventDate())
                .status(cake.getStatus())
                .estimatedPrice(cake.getEstimatedPrice())
                .createdAt(cake.getCreatedAt())
                // POLYMORPHIC CALLS:
                .eventSummary(cake.getEventSummary())
                .preparationLeadDays(cake.getPreparationLeadDays())
                .specialtyAddons(cake.getSpecialtyAddons());

        if (cake instanceof BirthdayCake b) {
            builder.cakeType("BIRTHDAY")
                   .ageNumber(b.getAgeNumber())
                   .candleSetIncluded(b.isCandleSetIncluded())
                   .characterTheme(b.getCharacterTheme());
        } else if (cake instanceof WeddingCake w) {
            builder.cakeType("WEDDING")
                   .tierCount(w.getTierCount())
                   .freshFlorals(w.isFreshFlorals())
                   .tastingKitOrdered(w.isTastingKitOrdered());
        } else if (cake instanceof CelebrationCake c) {
            builder.cakeType("CELEBRATION")
                   .celebrationType(c.getCelebrationType())
                   .edibleGoldLeaf(c.isEdibleGoldLeaf());
        }

        return builder.build();
    }
}
