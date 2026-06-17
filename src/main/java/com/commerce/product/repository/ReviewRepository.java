package com.commerce.product.repository;

import com.commerce.product.model.Review;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ReviewRepository extends MongoRepository<Review, String>, CustomReviewRepository {
}
