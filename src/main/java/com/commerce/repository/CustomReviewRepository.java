package com.commerce.repository;

import com.commerce.model.Review;

import java.util.List;

public interface CustomReviewRepository {
    List<Review> findReviewsByProductId(String productId);
    List<Review> findReviewsByRating(int rating);
}
