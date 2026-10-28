package com.crumb.order.entity;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@DiscriminatorValue("PICKUP")
@Getter
@Setter
@NoArgsConstructor
public class PickupOrder extends Order {

    @Column(name = "pickup_time_slot")
    private String pickupTimeSlot = "Immediate / 30 Minutes";

    @Column(name = "branch_location")
    private String branchLocation = "Crumb Flagship Bakery, Colombo 03";

    @Column(name = "curbside_vehicle_details")
    private String curbsideVehicleDetails;


    @Override
    public double calculateDeliveryFee() {
        return 0.0;
    }

    @Override
    public String getDeliverySummary() {
        return "Bakery Counter Pickup at " + branchLocation + " [Time Slot: " + pickupTimeSlot + "]";
    }

    @Override
    public String getFulfillmentTimeEstimate() {
        return "Packed and ready at bakery pickup counter in 20-30 minutes";
    }
}
