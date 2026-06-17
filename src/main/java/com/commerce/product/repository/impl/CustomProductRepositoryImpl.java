package com.commerce.product.repository.impl;

import com.commerce.product.model.Product;
import com.commerce.product.repository.CustomProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class CustomProductRepositoryImpl implements CustomProductRepository {
    private final MongoTemplate mongoTemplate;

    @Override
    public List<Product> findProductsByPriceRange(double minPrice, double maxPrice) {
        return mongoTemplate.find(new Query(Criteria.where("price").gte(minPrice).lte(maxPrice)), Product.class);
    }

    @Override
    public List<Product> findProductsByCategory(String category) {
        return mongoTemplate.find(new Query(Criteria.where("categories").is(category)), Product.class);
    }
}
