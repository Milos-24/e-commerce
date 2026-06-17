package com.commerce.order.dto;

import com.commerce.order.model.OrderStatus;
import com.commerce.order.model.PaymentStatus;
import com.commerce.order.model.ShippingAddress;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data @AllArgsConstructor @NoArgsConstructor @Builder
public class OrderResponse {
    private String id;
    private String customerId;
    private List<LineItemResponse> items;
    private double subtotal;
    private double shippingCost;
    private double tax;
    private double total;
    private String currency;
    private ShippingAddress shippingAddress;
    private OrderStatus status;
    private PaymentStatus paymentStatus;
    private Instant createdAt;
    private Instant updatedAt;
}
