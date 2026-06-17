package com.commerce.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data @AllArgsConstructor @NoArgsConstructor @Builder
public class LineItemResponse {
    private String productId;
    private String productName;
    private double unitPrice;
    private String brand;
    private List<String> categories;
    private Map<String, Object> attributes;
    private int quantity;
    private double lineSubtotal;
}
