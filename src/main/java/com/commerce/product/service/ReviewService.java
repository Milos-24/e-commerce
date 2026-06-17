package com.commerce.product.service;

import com.commerce.product.model.Review;
import com.commerce.product.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepository;

    public List<Review> getAllReviews() { return reviewRepository.findAll(); }
    public Optional<Review> getReviewById(String id) { return reviewRepository.findById(id); }
    public Review saveReview(Review review) { return reviewRepository.save(review); }
    public void deleteReview(String id) { reviewRepository.deleteById(id); }
    public List<Review> getReviewsByProductId(String productId) { return reviewRepository.findReviewsByProductId(productId); }
    public List<Review> getReviewsByRating(int rating) { return reviewRepository.findReviewsByRating(rating); }
}
