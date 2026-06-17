package com.commerce.catalog.category.repository;

import com.commerce.catalog.category.model.Category;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CategoryRepository extends MongoRepository<Category, String> {
}
