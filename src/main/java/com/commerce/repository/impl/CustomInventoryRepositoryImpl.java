package com.commerce.repository.impl;

import com.commerce.model.Inventory;
import com.commerce.repository.CustomInventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@RequiredArgsConstructor
@Repository
public class CustomInventoryRepositoryImpl implements CustomInventoryRepository {

    private final MongoTemplate mongoTemplate;

    @Override
    public List<Inventory> findInventoryByProductId(String productId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("productId").is(productId));
        return mongoTemplate.find(query, Inventory.class);
    }
}
