package com.crumb.customcake.entity;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * CelebrationCake - Custom cake entity for graduations, anniversaries, baby showers, and milestones.
 * 
 * // INHERITANCE:
 * Subclasses CustomCake with cake_type = 'CELEBRATION'.
 * 
 * // POLYMORPHISM:
 * Implements price estimation factoring in 24k edible gold leaf, custom milestone plaques,
 * and 3-day lead time.
 */
// INHERITANCE
@Entity
@DiscriminatorValue("CELEBRATION")
@Getter
@Setter
@NoArgsConstructor
public class CelebrationCake extends CustomCake {

    // ENCAPSULATION: private fields with Lombok accessors
    @Column(name = "celebration_type")
    private String celebrationType = "Anniversary"; // Anniversary, Graduation, Baby Shower, Corporate

    @Column(name = "edible_gold_leaf")
    private boolean edibleGoldLeaf = true;

    /**
     * // POLYMORPHISM:
     * Calculates milestone event pricing with metallic finishes and custom plaques.
     */
    // POLYMORPHISM
    @Override
    public double calculateEstimatedPrice() {
        double price = 65.00; // Base 6-inch

        String size = getSizeTiers() != null ? getSizeTiers().toLowerCase() : "";
        if (size.contains("8-inch")) price = 85.00;
        else if (size.contains("10-inch")) price = 110.00;
        else if (size.contains("2-tier")) price = 165.00;

        if (edibleGoldLeaf) {
            price += 15.00; // 24-Karat edible gold leaf flakes
        }

        if (getInscription() != null && !getInscription().trim().isEmpty()) {
            price += 5.00; // Custom acrylic plaque or gold script
        }

        return Math.round(price * 100.0) / 100.0;
    }

    // POLYMORPHISM
    @Override
    public String getEventSummary() {
        return celebrationType + " Milestone Celebration";
    }

    // POLYMORPHISM
    @Override
    public int getPreparationLeadDays() {
        return 3;
    }

    // POLYMORPHISM
    @Override
    public String getSpecialtyAddons() {
        return "Edible 24k Gold Leaf Highlights, Acrylic Keepsake Topper & Luxury Ribbon Wrap";
    }
}
