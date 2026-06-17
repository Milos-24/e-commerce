package com.commerce.product.repository;

import com.commerce.product.model.Product;

import java.util.List;

public interface CustomProductRepository {
    List<Product> findProductsByPriceRange(double minPrice, double maxPrice);
    List<Product> findProductsByCategory(String category);
}
