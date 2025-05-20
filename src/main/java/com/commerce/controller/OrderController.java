package com.commerce.controller;

import com.commerce.model.Order;
import com.commerce.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService orderService;

    @GetMapping
    public List<Order> getAllOrders() {
        return orderService.getAllOrders();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrderById(@PathVariable String id) {
        return orderService.getOrderById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Order createOrder(@RequestBody Order order) {
        return orderService.saveOrder(order);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable String id) {
        orderService.deleteOrder(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/customer/{customerId}")
    public List<Order> getOrdersByCustomerId(@PathVariable String customerId) {
        return orderService.getOrdersByCustomerId(customerId);
    }

    @GetMapping("/date-range")
    public List<Order> getOrdersWithinDateRange(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        return orderService.getOrdersWithinDateRange(startDate, endDate);
    }
}
