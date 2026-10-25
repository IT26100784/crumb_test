package com.crumb.order.service;

import com.crumb.common.exception.BadRequestException;
import com.crumb.common.exception.ForbiddenException;
import com.crumb.common.exception.ResourceNotFoundException;
import com.crumb.customer.entity.Customer;
import com.crumb.customer.service.CustomerService;
import com.crumb.order.dto.OrderCreateRequest;
import com.crumb.order.dto.OrderItemRequest;
import com.crumb.order.dto.OrderResponse;
import com.crumb.order.dto.OrderStatusUpdateRequest;
import com.crumb.order.entity.DeliveryOrder;
import com.crumb.order.entity.Order;
import com.crumb.order.entity.OrderItem;
import com.crumb.order.entity.PickupOrder;
import com.crumb.order.repository.OrderRepository;
import com.crumb.product.entity.BakeryProduct;
import com.crumb.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * OrderServiceImpl - Business implementation of OrderService.
 * 
 * WHY: Processes cart checkout, updates inventory, invokes polymorphic customer discounts
 * and delivery fees, and enforces fulfillment status workflows.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final CustomerService customerService;
    private final ProductService productService;

    /**
     * Places a customer order from cart items.
     * Demonstrates POLYMORPHISM across Customer (discounts), Product (volume pricing),
     * and Order (delivery fee calculation).
     */
    @Override
    public OrderResponse placeOrder(Long customerId, OrderCreateRequest request) {
        Customer customer = customerService.getCustomerEntity(customerId);

        Order order;
        String type = request.getOrderType() != null ? request.getOrderType().toUpperCase() : "DELIVERY";

        if ("PICKUP".equalsIgnoreCase(type)) {
            PickupOrder pickup = new PickupOrder();
            pickup.setPickupTimeSlot(request.getPickupTimeSlot() != null ? request.getPickupTimeSlot() : "Immediate / 30 Minutes");
            pickup.setBranchLocation(request.getBranchLocation() != null ? request.getBranchLocation() : "Crumb Flagship Bakery, Colombo 03");
            pickup.setCurbsideVehicleDetails(request.getCurbsideVehicleDetails());
            order = pickup;
        } else {
            DeliveryOrder delivery = new DeliveryOrder();
            delivery.setDeliveryAddress(request.getDeliveryAddress() != null ? request.getDeliveryAddress().trim() : customer.getAddress());
            delivery.setContactPhone(request.getContactPhone() != null ? request.getContactPhone().trim() : customer.getPhone());
            delivery.setSpecialDeliveryInstructions(request.getSpecialDeliveryInstructions());
            if (request.getDistanceKm() != null && request.getDistanceKm() > 0) {
                delivery.setDistanceKm(request.getDistanceKm());
            }
            order = delivery;
        }

        // Generate unique tracking order number
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        int rand = ThreadLocalRandom.current().nextInt(1000, 9999);
        order.setOrderNumber("CRUMB-" + datePart + "-" + rand);
        order.setCustomer(customer);
        order.setOrderNotes(request.getOrderNotes());
        order.setStatus("CONFIRMED");

        double subtotal = 0.0;

        for (OrderItemRequest itemReq : request.getItems()) {
            BakeryProduct product = productService.getProductEntity(itemReq.getProductId());

            if (product.getStockQuantity() < itemReq.getQuantity()) {
                throw new BadRequestException("Not enough stock for '" + product.getName() +
                        "'. Only " + product.getStockQuantity() + " available.");
            }

            // Decrement stock
            product.setStockQuantity(product.getStockQuantity() - itemReq.getQuantity());

            // POLYMORPHISM: Product calculates volume price
            double itemTotal = product.calculateFinalPrice(itemReq.getQuantity());
            double unitPrice = Math.round((itemTotal / itemReq.getQuantity()) * 100.0) / 100.0;

            OrderItem item = new OrderItem(order, product, itemReq.getQuantity(), unitPrice);
            order.addItem(item);
            subtotal += itemTotal;
        }

        subtotal = Math.round(subtotal * 100.0) / 100.0;
        order.setSubtotal(subtotal);

        // POLYMORPHISM: Customer calculates member discount
        double discount = customer.calculateDiscount(subtotal);
        order.setDiscountAmount(discount);

        // POLYMORPHISM: Order calculates delivery fee (zero for pickup, distance-based for delivery)
        double deliveryFee = order.calculateDeliveryFee();
        order.setDeliveryFee(deliveryFee);

        double total = Math.max(0.0, subtotal - discount + deliveryFee);
        order.setTotalAmount(Math.round(total * 100.0) / 100.0);

        // Award loyalty points to customer (1 point per $2 spent)
        int earnedPoints = (int) (total / 2.0);
        customer.setLoyaltyPoints(customer.getLoyaltyPoints() + earnedPoints);

        Order saved = orderRepository.save(order);
        return OrderResponse.fromEntity(saved);
    }

    /**
     * Retrieves all orders belonging to a customer.
     */
    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getCustomerOrders(Long customerId) {
        return orderRepository.findByCustomerIdOrderByCreatedAtDesc(customerId)
                .stream()
                .map(OrderResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Admin endpoint: list all orders with optional status and orderType filters.
     */
    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders(String status, String orderType) {
        List<Order> list;
        if (status != null && !status.trim().isEmpty() && !"ALL".equalsIgnoreCase(status)) {
            list = orderRepository.findByStatusOrderByCreatedAtDesc(status.trim().toUpperCase());
        } else {
            list = orderRepository.findAllByOrderByCreatedAtDesc();
        }

        return list.stream()
                .filter(o -> {
                    if (orderType == null || orderType.trim().isEmpty() || "ALL".equalsIgnoreCase(orderType)) {
                        return true;
                    }
                    if ("DELIVERY".equalsIgnoreCase(orderType)) return o instanceof DeliveryOrder;
                    if ("PICKUP".equalsIgnoreCase(orderType)) return o instanceof PickupOrder;
                    return true;
                })
                .map(OrderResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves order by primary key ID.
     */
    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long id) {
        return OrderResponse.fromEntity(getOrderEntity(id));
    }

    /**
     * Retrieves order by tracking reference number.
     */
    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderByNumber(String orderNumber) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with number: " + orderNumber));
        return OrderResponse.fromEntity(order);
    }

    /**
     * Retrieves raw Order entity by ID.
     */
    @Override
    @Transactional(readOnly = true)
    public Order getOrderEntity(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));
    }

    /**
     * Updates order workflow status (CONFIRMED -> BAKING -> READY -> DELIVERED/COMPLETED).
     */
    @Override
    public OrderResponse updateOrderStatus(Long id, OrderStatusUpdateRequest request) {
        Order order = getOrderEntity(id);
        order.setStatus(request.getStatus().trim().toUpperCase());
        Order saved = orderRepository.save(order);
        return OrderResponse.fromEntity(saved);
    }

    /**
     * Cancels an order and restocks inventory if preparation has not commenced.
     */
    @Override
    public OrderResponse cancelOrder(Long id, Long customerId, boolean isAdmin) {
        Order order = getOrderEntity(id);

        if (!isAdmin && !order.getCustomer().getId().equals(customerId)) {
            throw new ForbiddenException("You cannot cancel another customer's order.");
        }

        if ("BAKING".equalsIgnoreCase(order.getStatus()) || "READY".equalsIgnoreCase(order.getStatus()) ||
            "DELIVERED".equalsIgnoreCase(order.getStatus()) || "COMPLETED".equalsIgnoreCase(order.getStatus())) {
            throw new BadRequestException("Orders already baking, ready, or dispatched cannot be cancelled.");
        }

        order.setStatus("CANCELLED");

        // Restock inventory
        for (OrderItem item : order.getItems()) {
            BakeryProduct product = item.getProduct();
            product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
        }

        Order saved = orderRepository.save(order);
        return OrderResponse.fromEntity(saved);
    }

    /**
     * Deletes an order record (admin only).
     */
    @Override
    public void deleteOrder(Long id) {
        Order order = getOrderEntity(id);
        orderRepository.delete(order);
    }

    /**
     * Calculates total gross revenue.
     */
    @Override
    @Transactional(readOnly = true)
    public double getTotalRevenue() {
        return Math.round(orderRepository.calculateTotalRevenue() * 100.0) / 100.0;
    }
}
