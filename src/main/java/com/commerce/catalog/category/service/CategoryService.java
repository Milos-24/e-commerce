package com.commerce.catalog.category.service;

import com.commerce.catalog.category.model.Category;
import com.commerce.catalog.category.repository.CategoryRepository;
import com.commerce.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    @Cacheable("categories")
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public Optional<Category> getCategoryById(String id) {
        return categoryRepository.findById(id);
    }

    @CacheEvict(value = "categories", allEntries = true)
    public Category saveCategory(Category category) {
        return categoryRepository.save(category);
    }

    @CacheEvict(value = "categories", allEntries = true)
    public void deleteCategory(String id) {
        long refs = productRepository.countByCategoriesContaining(id);
        if (refs > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Cannot delete category '" + id + "': referenced by " + refs + " product(s)");
        }
        categoryRepository.deleteById(id);
    }

    public List<Category> findAllByIds(List<String> ids) {
        return categoryRepository.findAllById(ids);
    }
}
