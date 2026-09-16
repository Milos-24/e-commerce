package com.commerce.order.repository.impl;

import com.commerce.order.model.Order;
import com.commerce.order.model.OrderStatus;
import com.commerce.order.repository.CustomOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class CustomOrderRepositoryImpl implements CustomOrderRepository {

    private final MongoTemplate mongoTemplate;

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("createdAt", "total", "status");

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

    @Override
    public Page<Order> findOrdersFiltered(int page, int size, String sortField, String sortDir,
                                          OrderStatus status, String customerName, String email) {
        List<Criteria> conditions = new ArrayList<>();

        if (status != null) {
            conditions.add(Criteria.where("status").is(status));
        }
        if (customerName != null && !customerName.isBlank()) {
            conditions.add(Criteria.where("shippingAddress.fullName").regex(customerName, "i"));
        }
        if (email != null && !email.isBlank()) {
            conditions.add(Criteria.where("customerId").regex(email, "i"));
        }

        Criteria criteria = conditions.isEmpty()
                ? new Criteria()
                : new Criteria().andOperator(conditions.toArray(new Criteria[0]));

        String field = ALLOWED_SORT_FIELDS.contains(sortField) ? sortField : "createdAt";
        Sort sort = Sort.by("asc".equalsIgnoreCase(sortDir) ? Sort.Direction.ASC : Sort.Direction.DESC, field);
        PageRequest pageable = PageRequest.of(page, size, sort);

        Query query = new Query(criteria).with(pageable);
        long total = mongoTemplate.count(new Query(criteria), Order.class);
        List<Order> orders = mongoTemplate.find(query, Order.class);

        return new PageImpl<>(orders, pageable, total);
    }
}
