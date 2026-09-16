package com.commerce.product.repository.impl;

import com.commerce.product.model.Product;
import com.commerce.product.repository.CustomProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class CustomProductRepositoryImpl implements CustomProductRepository {

    private final MongoTemplate mongoTemplate;

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("price", "name", "brand");

    @Override
    public List<Product> findProductsByPriceRange(double minPrice, double maxPrice) {
        return mongoTemplate.find(new Query(Criteria.where("price").gte(minPrice).lte(maxPrice)), Product.class);
    }

    @Override
    public List<Product> findProductsByCategory(String category) {
        return mongoTemplate.find(new Query(Criteria.where("categories").is(category)), Product.class);
    }

    @Override
    public Page<Product> findProductsFiltered(int page, int size, String sortField, String sortDir,
                                              String category, String brand, Double minPrice, Double maxPrice) {
        List<Criteria> conditions = new ArrayList<>();

        if (category != null && !category.isBlank()) {
            conditions.add(Criteria.where("categories").is(category));
        }
        if (brand != null && !brand.isBlank()) {
            conditions.add(Criteria.where("brand").is(brand));
        }
        if (minPrice != null || maxPrice != null) {
            Criteria priceCriteria = Criteria.where("price");
            if (minPrice != null) priceCriteria = priceCriteria.gte(minPrice);
            if (maxPrice != null) priceCriteria = priceCriteria.lte(maxPrice);
            conditions.add(priceCriteria);
        }

        Criteria criteria = conditions.isEmpty()
                ? new Criteria()
                : new Criteria().andOperator(conditions.toArray(new Criteria[0]));

        String field = ALLOWED_SORT_FIELDS.contains(sortField) ? sortField : "name";
        Sort sort = Sort.by("desc".equalsIgnoreCase(sortDir) ? Sort.Direction.DESC : Sort.Direction.ASC, field);
        PageRequest pageable = PageRequest.of(page, size, sort);

        long total = mongoTemplate.count(new Query(criteria), Product.class);
        List<Product> products = mongoTemplate.find(new Query(criteria).with(pageable), Product.class);

        return new PageImpl<>(products, pageable, total);
    }
}