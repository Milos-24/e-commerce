package com.commerce.order.dto;

import com.commerce.order.model.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @AllArgsConstructor @NoArgsConstructor
public class UpdateOrderStatusRequest {
    @NotNull private OrderStatus status;
}
