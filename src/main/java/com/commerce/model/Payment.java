package com.commerce.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Payment {
    private String paymentId;
    private String method;
    private String status;
    private double amount;
}

