package com.commerce.repository;

import com.commerce.model.Order;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface OrderRepository extends MongoRepository<Order, String>, CustomOrderRepository {
}
