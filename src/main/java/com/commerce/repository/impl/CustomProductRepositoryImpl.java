package com.commerce.repository.impl;

import com.commerce.model.Product;
import com.commerce.repository.CustomProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Repository;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;

@RequiredArgsConstructor
@Repository
public class CustomProductRepositoryImpl implements CustomProductRepository {

    private final MongoTemplate mongoTemplate;

    @Override
    public List<Product> findProductsByPriceRange(double minPrice, double maxPrice) {
        Query query = new Query();
        query.addCriteria(Criteria.where("price").gte(minPrice).lte(maxPrice));
        return mongoTemplate.find(query, Product.class);
    }

    @Override
    public List<Product> findProductsByCategory(String category) {
        Query query = new Query();
        query.addCriteria(Criteria.where("categories").is(category));
        return mongoTemplate.find(query, Product.class);
    }
}
