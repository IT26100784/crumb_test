package com.crumb.customcake.entity;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * BirthdayCake - Bespoke birthday cake entity.
 * 
 * // INHERITANCE:
 * Subclasses CustomCake with cake_type = 'BIRTHDAY'.
 * 
 * // POLYMORPHISM:
 * Implements price estimation factoring in age candle sets, thematic toppers,
 * and 2-day kitchen preparation notice.
 */
// INHERITANCE
@Entity
@DiscriminatorValue("BIRTHDAY")
@Getter
@Setter
@NoArgsConstructor
public class BirthdayCake extends CustomCake {

    // ENCAPSULATION: private fields with Lombok accessors
    @Column(name = "age_number")
    private Integer ageNumber;

    @Column(name = "candles_included")
    private boolean candleSetIncluded = true;

    @Column(name = "character_theme")
    private String characterTheme = "Confetti Festive";

    /**
     * // POLYMORPHISM:
     * Calculates realistic price based on cake diameter/tiers, frosting, and birthday decorations.
     */
    // POLYMORPHISM
    @Override
    public double calculateEstimatedPrice() {
        double price = 5500.00; // Base 6-inch

        String size = getSizeTiers() != null ? getSizeTiers().toLowerCase() : "";
        if (size.contains("8-inch") || size.contains("1")) price = 6500.00;
        else if (size.contains("10-inch")) price = 8500.00;
        else if (size.contains("2-tier") || size.contains("2")) price = 12500.00;
        else if (size.contains("3-tier") || size.contains("3")) price = 21000.00;
        else if (size.contains("4")) price = 32000.00;
        else if (size.contains("5")) price = 45000.00;

        if (getFrosting() != null && getFrosting().toLowerCase().contains("fondant")) {
            price += 1800.00;
        }

        if (candleSetIncluded) {
            price += 450.00;
        }

        if (getInscription() != null && !getInscription().trim().isEmpty()) {
            price += 300.00;
        }

        return Math.round(price * 100.0) / 100.0;
    }

    // POLYMORPHISM
    @Override
    public String getEventSummary() {
        return "Birthday Celebration" + (ageNumber != null ? " (Turning " + ageNumber + "!)" : "")
                + " - Theme: " + characterTheme;
    }

    // POLYMORPHISM
    @Override
    public int getPreparationLeadDays() {
        return 2;
    }

    // POLYMORPHISM
    @Override
    public String getSpecialtyAddons() {
        return "Artisan Sparkler Candles, Number Topper & Birthday Presentation Box";
    }
}
