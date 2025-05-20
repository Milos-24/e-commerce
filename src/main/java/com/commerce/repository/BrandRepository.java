package com.commerce.repository;

import com.commerce.model.Brand;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface BrandRepository extends MongoRepository<Brand, String> {
}
