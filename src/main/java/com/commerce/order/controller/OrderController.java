package com.commerce.order.controller;

import com.commerce.order.dto.OrderResponse;
import com.commerce.order.dto.PlaceOrderRequest;
import com.commerce.order.dto.UpdateOrderStatusRequest;
import com.commerce.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    @CrossOrigin
    public List<OrderResponse> getAllOrders() {
        return orderService.getAllOrders();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @CrossOrigin
    public OrderResponse placeOrder(@Valid @RequestBody PlaceOrderRequest request) {
        return orderService.placeOrder(request);
    }

    @GetMapping("/{id}")
    @CrossOrigin
    public OrderResponse getOrderById(@PathVariable String id) {
        return orderService.getOrderById(id);
    }

    @CrossOrigin
    @GetMapping("/customer/{customerId}")
    public Page<OrderResponse> getCustomerOrders(@PathVariable String customerId,
                                                  @RequestParam(defaultValue = "0") int page,
                                                  @RequestParam(defaultValue = "20") int size) {
        return orderService.getCustomerOrders(customerId, page, size);
    }

    @CrossOrigin
    @PatchMapping("/{id}/status")
    public OrderResponse updateStatus(@PathVariable String id,
                                      @Valid @RequestBody UpdateOrderStatusRequest request) {
        return orderService.updateStatus(id, request.getStatus());
    }
}
