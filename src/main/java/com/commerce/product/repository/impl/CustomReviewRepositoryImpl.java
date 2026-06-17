package com.commerce.product.repository.impl;

import com.commerce.product.model.Review;
import com.commerce.product.repository.CustomReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class CustomReviewRepositoryImpl implements CustomReviewRepository {
    private final MongoTemplate mongoTemplate;

    @Override
    public List<Review> findReviewsByProductId(String productId) {
        return mongoTemplate.find(new Query(Criteria.where("productId").is(productId)), Review.class);
    }

    @Override
    public List<Review> findReviewsByRating(int rating) {
        return mongoTemplate.find(new Query(Criteria.where("rating").is(rating)), Review.class);
    }
}
