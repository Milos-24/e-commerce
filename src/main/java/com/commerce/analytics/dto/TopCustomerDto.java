package com.commerce.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @AllArgsConstructor @NoArgsConstructor @Builder
public class TopCustomerDto {
    private String customerId;
    private double totalSpend;
    private long orderCount;
}
