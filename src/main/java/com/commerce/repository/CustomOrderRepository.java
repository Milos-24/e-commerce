package com.commerce.repository;

import com.commerce.model.Order;

import java.util.List;

public interface CustomOrderRepository {
    List<Order> findOrdersByCustomerId(String customerId);
    List<Order> findOrdersWithinDateRange(String startDate, String endDate);
}

