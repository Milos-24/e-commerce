package com.commerce.order.repository.impl;

import com.commerce.order.model.Order;
import com.commerce.order.repository.CustomOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class CustomOrderRepositoryImpl implements CustomOrderRepository {
    private final MongoTemplate mongoTemplate;

    @Override
    public List<Order> findOrdersByCustomerId(String customerId) {
        return mongoTemplate.find(new Query(Criteria.where("customerId").is(customerId)), Order.class);
    }

    @Override
    public List<Order> findOrdersWithinDateRange(String startDate, String endDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate start = LocalDate.parse(startDate, formatter);
        LocalDate end = LocalDate.parse(endDate, formatter);
        return mongoTemplate.find(
                new Query(Criteria.where("createdAt").gte(start).lte(end)), Order.class);
    }
}
