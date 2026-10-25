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
        double price = 55.00; // Base 6-inch

        String size = getSizeTiers() != null ? getSizeTiers().toLowerCase() : "";
        if (size.contains("8-inch")) price = 75.00;
        else if (size.contains("10-inch")) price = 95.00;
        else if (size.contains("2-tier")) price = 145.00;
        else if (size.contains("3-tier")) price = 210.00;

        if (getFrosting() != null && getFrosting().toLowerCase().contains("fondant")) {
            price += 25.00;
        }

        if (candleSetIncluded) {
            price += 5.00;
        }

        if (getInscription() != null && !getInscription().trim().isEmpty()) {
            price += 4.00;
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
