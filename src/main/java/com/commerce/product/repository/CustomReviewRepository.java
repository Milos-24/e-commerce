package com.commerce.product.repository;

import com.commerce.product.model.Review;

import java.util.List;

public interface CustomReviewRepository {
    List<Review> findReviewsByProductId(String productId);
    List<Review> findReviewsByRating(int rating);
}
