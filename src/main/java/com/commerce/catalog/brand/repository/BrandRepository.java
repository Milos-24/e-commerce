package com.commerce.catalog.brand.repository;

import com.commerce.catalog.brand.model.Brand;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface BrandRepository extends MongoRepository<Brand, String> {
}
