package com.crumb.order.dto;

import com.crumb.order.entity.DeliveryOrder;
import com.crumb.order.entity.Order;
import com.crumb.order.entity.PickupOrder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * OrderResponse - Complete customer/admin order receipt presentation DTO.
 * 
 * WHY: Presents line items, calculated subtotal, delivery charges, customer info,
 * and polymorphic fulfillment details (summary, estimated fulfillment time).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {

    // ENCAPSULATION: private fields with getters/setters
    private Long id;
    private String orderNumber;
    private Long customerId;
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private List<OrderItemResponse> items;
    private double subtotal;
    private double discountAmount;
    private double deliveryFee;
    private double totalAmount;
    private String status;
    private String orderNotes;
    private String orderType;
    private LocalDateTime createdAt;

    // POLYMORPHIC calculated fields
    private String deliverySummary;
    private String fulfillmentEstimate;

    // Delivery-specific details
    private String deliveryAddress;
    private String contactPhone;
    private String specialDeliveryInstructions;

    // Pickup-specific details
    private String pickupTimeSlot;
    private String branchLocation;
    private String curbsideVehicleDetails;

    /**
     * Converts an Order entity into an OrderResponse DTO.
     * Demonstrates POLYMORPHISM by calling polymorphic methods on the Order instance.
     */
    public static OrderResponse fromEntity(Order order) {
        if (order == null) return null;

        List<OrderItemResponse> itemResponses = order.getItems() != null
                ? order.getItems().stream().map(OrderItemResponse::fromEntity).collect(Collectors.toList())
                : List.of();

        OrderResponseBuilder builder = OrderResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .customerId(order.getCustomer() != null ? order.getCustomer().getId() : null)
                .customerName(order.getCustomer() != null ? order.getCustomer().getFullName() : "N/A")
                .customerEmail(order.getCustomer() != null ? order.getCustomer().getEmail() : "N/A")
                .customerPhone(order.getCustomer() != null ? order.getCustomer().getPhone() : "N/A")
                .items(itemResponses)
                .subtotal(order.getSubtotal())
                .discountAmount(order.getDiscountAmount())
                .deliveryFee(order.getDeliveryFee())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .orderNotes(order.getOrderNotes())
                .createdAt(order.getCreatedAt())
                // POLYMORPHIC CALLS:
                .deliverySummary(order.getDeliverySummary())
                .fulfillmentEstimate(order.getFulfillmentTimeEstimate());

        if (order instanceof DeliveryOrder d) {
            builder.orderType("DELIVERY")
                   .deliveryAddress(d.getDeliveryAddress())
                   .contactPhone(d.getContactPhone())
                   .specialDeliveryInstructions(d.getSpecialDeliveryInstructions());
        } else if (order instanceof PickupOrder p) {
            builder.orderType("PICKUP")
                   .pickupTimeSlot(p.getPickupTimeSlot())
                   .branchLocation(p.getBranchLocation())
                   .curbsideVehicleDetails(p.getCurbsideVehicleDetails());
        }

        return builder.build();
    }
}
