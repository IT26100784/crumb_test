package com.crumb.customcake.entity;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * WeddingCake - Bespoke tiered wedding cake centerpiece entity.
 * 
 * // INHERITANCE:
 * Subclasses CustomCake with cake_type = 'WEDDING'.
 * 
 * // POLYMORPHISM:
 * Implements price estimation factoring in tiered structural engineering,
 * fresh floral installations, and 7-day master pastry chef lead time.
 */
// INHERITANCE
@Entity
@DiscriminatorValue("WEDDING")
@Getter
@Setter
@NoArgsConstructor
public class WeddingCake extends CustomCake {

    // ENCAPSULATION: private fields with Lombok accessors
    @Column(name = "tier_count")
    private int tierCount = 3;

    @Column(name = "has_fresh_florals")
    private boolean freshFlorals = true;

    @Column(name = "tasting_kit_ordered")
    private boolean tastingKitOrdered = false;

    /**
     * // POLYMORPHISM:
     * Calculates luxury tiered wedding cake quotation including internal food-grade doweling
     * and floral arrangement setup.
     */
    // POLYMORPHISM
    @Override
    public double calculateEstimatedPrice() {
        double price = 160.00; // Base 2-tier wedding cake

        if (tierCount >= 4) {
            price = 380.00;
        } else if (tierCount == 3) {
            price = 270.00;
        }

        if (freshFlorals) {
            price += 45.00; // Sugar flowers and organic pesticide-free floral cascade
        }

        if (tastingKitOrdered) {
            price += 25.00; // Sample box delivered to couple prior to event
        }

        if (getFrosting() != null && getFrosting().toLowerCase().contains("fondant")) {
            price += 40.00; // Hand-smoothed luxury satin fondant
        }

        return Math.round(price * 100.0) / 100.0;
    }

    // POLYMORPHISM
    @Override
    public String getEventSummary() {
        return "Wedding Reception Nuptials (" + tierCount + " Grand Tiered Architecture)";
    }

    // POLYMORPHISM
    @Override
    public int getPreparationLeadDays() {
        return 7; // Requires 7 days advance notice for structural curing
    }

    // POLYMORPHISM
    @Override
    public String getSpecialtyAddons() {
        return "Food-Grade Structural Doweling, Floral Cascade Styling & On-Site Cake Cutting Guide";
    }
}
