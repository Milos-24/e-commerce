package com.commerce.order.repository;

import com.commerce.order.model.Order;

import java.util.List;

public interface CustomOrderRepository {
    List<Order> findOrdersByCustomerId(String customerId);
    List<Order> findOrdersWithinDateRange(String startDate, String endDate);
}
