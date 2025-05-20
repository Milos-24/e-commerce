package com.commerce.repository.impl;

import com.commerce.model.Order;
import com.commerce.repository.CustomOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Repository;
import org.springframework.data.mongodb.core.query.Query;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RequiredArgsConstructor
@Repository
public class CustomOrderRepositoryImpl implements CustomOrderRepository {

    private final MongoTemplate mongoTemplate;

    @Override
    public List<Order> findOrdersByCustomerId(String customerId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("customerId").is(customerId));
        return mongoTemplate.find(query, Order.class);
    }

    @Override
    public List<Order> findOrdersWithinDateRange(String startDate, String endDate) {
        Query query = new Query();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate start = LocalDate.parse(startDate, formatter);
        LocalDate end = LocalDate.parse(endDate, formatter);
        query.addCriteria(Criteria.where("orderDate").gte(start).lte(end));
        return mongoTemplate.find(query, Order.class);
    }
}