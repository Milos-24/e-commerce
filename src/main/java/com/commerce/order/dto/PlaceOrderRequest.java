package com.commerce.order.dto;

import com.commerce.order.model.ShippingAddress;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data @AllArgsConstructor @NoArgsConstructor
public class PlaceOrderRequest {
    @NotBlank private String customerId;
    @NotEmpty @Valid private List<OrderItemRequest> items;
    @NotNull @Valid private ShippingAddress shippingAddress;
    private String currency = "USD";
}
