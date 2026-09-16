package com.commerce.order.repository;

import com.commerce.order.model.Order;
import com.commerce.order.model.OrderStatus;
import org.springframework.data.domain.Page;

import java.util.List;

public interface CustomOrderRepository {
    List<Order> findOrdersByCustomerId(String customerId);
    List<Order> findOrdersWithinDateRange(String startDate, String endDate);
    Page<Order> findOrdersFiltered(int page, int size, String sortField, String sortDir,
                                   OrderStatus status, String customerName, String email);
}
