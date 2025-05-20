package com.commerce.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(collection = "orders")
public class Order {
    @Id
    private String id;
    private String customerId; // References Customer collection
    private List<OrderItem> items;
    private String status;
    private Payment payment;   // Embedded payment details
}

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
class OrderItem {
    private String productId; // References Product collection
    private int quantity;
}

