package com.commerce.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @AllArgsConstructor @NoArgsConstructor @Builder
public class OrderSummaryDto {
    private long totalOrders;
    private double totalRevenue;
    private double averageOrderValue;
}
