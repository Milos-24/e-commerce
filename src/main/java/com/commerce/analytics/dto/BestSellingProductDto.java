package com.commerce.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @AllArgsConstructor @NoArgsConstructor @Builder
public class BestSellingProductDto {
    private String productId;
    private String productName;
    private long totalQuantity;
    private double totalRevenue;
}
