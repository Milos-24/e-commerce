package com.commerce.order.model;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @AllArgsConstructor @NoArgsConstructor @Builder
public class ShippingAddress {
    @NotBlank private String fullName;
    @NotBlank private String street;
    @NotBlank private String city;
    @NotBlank private String state;
    @NotBlank private String zip;
    @NotBlank private String country;
}
