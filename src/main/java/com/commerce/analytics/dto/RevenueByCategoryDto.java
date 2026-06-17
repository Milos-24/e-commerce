package com.commerce.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @AllArgsConstructor @NoArgsConstructor @Builder
public class RevenueByCategoryDto {
    private String category;
    private double revenue;
    private long totalQuantity;
}
