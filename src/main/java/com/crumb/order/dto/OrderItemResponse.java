package com.crumb.order.dto;

import com.crumb.order.entity.OrderItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * OrderItemResponse - Output presentation for a purchased product line item.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemResponse {

    // ENCAPSULATION: private fields with getters/setters
    private Long id;
    private Long productId;
    private String productName;
    private String imageUrl;
    private int quantity;
    private double unitPrice;
    private double itemSubtotal;

    public static OrderItemResponse fromEntity(OrderItem item) {
        if (item == null) return null;
        return OrderItemResponse.builder()
                .id(item.getId())
                .productId(item.getProduct() != null ? item.getProduct().getId() : null)
                .productName(item.getProductName())
                .imageUrl(item.getProduct() != null ? item.getProduct().getImageUrl() : "")
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .itemSubtotal(item.getItemSubtotal())
                .build();
    }
}
