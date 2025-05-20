package com.commerce.repository.impl;

import com.commerce.model.Review;
import com.commerce.repository.CustomReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Repository;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;

@RequiredArgsConstructor
@Repository
public class CustomReviewRepositoryImpl implements CustomReviewRepository {

    private final MongoTemplate mongoTemplate;

    @Override
    public List<Review> findReviewsByProductId(String productId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("productId").is(productId));
        return mongoTemplate.find(query, Review.class);
    }

    @Override
    public List<Review> findReviewsByRating(int rating) {
        Query query = new Query();
        query.addCriteria(Criteria.where("rating").is(rating));
        return mongoTemplate.find(query, Review.class);
    }
}
