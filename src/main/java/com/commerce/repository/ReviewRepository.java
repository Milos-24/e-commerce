package com.commerce.repository;

import com.commerce.model.Review;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ReviewRepository extends MongoRepository<Review, String>, CustomReviewRepository {
}
