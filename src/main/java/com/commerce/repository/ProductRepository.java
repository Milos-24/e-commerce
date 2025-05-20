package com.commerce.repository;

import com.commerce.model.Product;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ProductRepository extends MongoRepository<Product, String>, CustomProductRepository {
}
