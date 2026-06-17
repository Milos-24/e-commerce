package com.commerce.order.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Data @AllArgsConstructor @NoArgsConstructor @Builder
@Document(collection = "orders")
@CompoundIndexes({
        @CompoundIndex(name = "customer_created", def = "{'customerId': 1, 'createdAt': -1}"),
        @CompoundIndex(name = "status_created",   def = "{'status': 1, 'createdAt': -1}")
})
public class Order {
    @Id private String id;
    private String customerId;
    private List<LineItem> items;
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
