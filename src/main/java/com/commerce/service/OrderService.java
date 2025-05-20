package com.commerce.service;

import com.commerce.model.Order;
import com.commerce.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class OrderService {

    private final OrderRepository orderRepository;

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public Optional<Order> getOrderById(String id) {
        return orderRepository.findById(id);
    }

    public Order saveOrder(Order order) {
        return orderRepository.save(order);
    }

    public void deleteOrder(String id) {
        orderRepository.deleteById(id);
    }

    public List<Order> getOrdersByCustomerId(String customerId) {
        return orderRepository.findOrdersByCustomerId(customerId);
    }

    public List<Order> getOrdersWithinDateRange(String startDate, String endDate) {
        return orderRepository.findOrdersWithinDateRange(startDate, endDate);
    }
}
