package com.commerce.order.repository;

import com.commerce.order.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface OrderRepository extends MongoRepository<Order, String>, CustomOrderRepository {
    Page<Order> findByCustomerId(String customerId, Pageable pageable);
}
