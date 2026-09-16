package com.commerce.product.repository;

import com.commerce.product.model.Product;
import org.springframework.data.domain.Page;

import java.util.List;

public interface CustomProductRepository {
    List<Product> findProductsByPriceRange(double minPrice, double maxPrice);
    List<Product> findProductsByCategory(String category);
    Page<Product> findProductsFiltered(int page, int size, String sortField, String sortDir,
                                       String category, String brand, Double minPrice, Double maxPrice);
}
