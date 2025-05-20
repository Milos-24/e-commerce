package com.commerce.service;

import com.commerce.model.Product;
import com.commerce.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class ProductService {
    private final ProductRepository productRepository;

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Optional<Product> getProductById(String id) {
        return productRepository.findById(id);
    }

    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }

    public void deleteProduct(String id) {
        productRepository.deleteById(id);
    }

    public List<Product> getProductsByPriceRange(double minPrice, double maxPrice) {
        return productRepository.findProductsByPriceRange(minPrice, maxPrice);
    }

    public List<Product> getProductsByCategory(String category) {
        return productRepository.findProductsByCategory(category);
    }

}
