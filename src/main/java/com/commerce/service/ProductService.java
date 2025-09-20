package com.commerce.service;

import com.commerce.model.Product;
import com.commerce.repository.ProductRepository;
import com.commerce.s3.S3Buckets;
import com.commerce.s3.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Service
@Slf4j
public class ProductService {
    private final ProductRepository productRepository;
    private final S3Service s3Service;
    private final S3Buckets s3Buckets;

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


    public byte[] getImage(String productId) {

        Optional<Product> product = productRepository.findById(productId);

        if (product.isPresent()) {
            Product selectedProduct = product.get();

            return s3Service.getObject(s3Buckets.getEcomm(), "images/" + selectedProduct.getImageUrls().stream().findFirst().get());
        } else {
            log.error("Product with id {} not found", productId);
            return null;
        }

    }


    public void uploadImage(String productId, MultipartFile file) {
        try {
            String imageId = UUID.randomUUID().toString();
            s3Service.putObject(
                    s3Buckets.getEcomm(),
                    "images/%s/%s".formatted(productId, imageId),
                    file.getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Product createProductWithImage(Product product, MultipartFile file) {
        try {
            // 1. Save the product first (so it has an ID)
            Product saved = productRepository.save(product);

            // 2. Create a unique key for S3
            String key = "images/" + saved.getId() + "/" + UUID.randomUUID();

            // 3. Upload image to S3
            s3Service.putObject(s3Buckets.getEcomm(), key, file.getBytes());

            // 4. Update product with image key
            saved.getImageUrls().add(key);
            return productRepository.save(saved);
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload image", e);
        }
    }

}
