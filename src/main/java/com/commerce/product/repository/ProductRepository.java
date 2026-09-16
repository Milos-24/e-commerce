package com.commerce.product.repository;

import com.commerce.product.model.Product;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ProductRepository extends MongoRepository<Product, String>, CustomProductRepository {
    long countByCategoriesContaining(String category);
}
