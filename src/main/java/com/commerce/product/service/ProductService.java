package com.commerce.product.service;

import com.commerce.product.model.Product;
import com.commerce.product.repository.ProductRepository;
import com.commerce.shared.s3.S3Buckets;
import com.commerce.shared.s3.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.ArrayList;
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
    private final MongoTemplate mongoTemplate;
    private final EmbeddingService embeddingService;

    private static final String VECTOR_INDEX = "vector_index";

    public List<Product> getAllProducts() {
        return productRepository.findAll().stream()
                .peek(this::resolveImageUrls)
                .toList();
    }

    public Optional<Product> getProductById(String id) {
        Product product = productRepository.findById(id).orElse(new Product());
        resolveImageUrls(product);
        return Optional.of(product);
    }

    /** Used by OrderService to get raw product data for price snapshots — no S3 URL generation. */
    public Product getProductForOrder(String id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found: " + id));
    }

    public Product saveProduct(Product product) {
        attachEmbedding(product);
        return productRepository.save(product);
    }

    public void deleteProduct(String id) {
        productRepository.deleteById(id);
    }

    public List<Product> getProductsByPriceRange(double minPrice, double maxPrice) {
        return productRepository.findProductsByPriceRange(minPrice, maxPrice).stream()
                .peek(this::resolveImageUrls)
                .toList();
    }

    public List<Product> getProductsByCategory(String category) {
        return productRepository.findProductsByCategory(category).stream()
                .peek(this::resolveImageUrls)
                .toList();
    }

    public void uploadImage(String productId, MultipartFile file) {
        try {
            s3Service.putObject(s3Buckets.getEcomm(),
                    "images/%s/%s".formatted(productId, UUID.randomUUID()),
                    file.getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Product createProductWithImage(Product product, MultipartFile file) {
        try {
            Product saved = productRepository.save(product);
            saved.setImageUrls(new ArrayList<>());
            String key = "images/" + saved.getId() + "/" + UUID.randomUUID();
            s3Service.putObject(s3Buckets.getEcomm(), key, file.getBytes());
            saved.getImageUrls().add(key);
            attachEmbedding(saved);
            return productRepository.save(saved);
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload image", e);
        }
    }

    public List<Product> vectorSearch(String query, int limit) {
        List<Double> queryVector = embeddingService.embed(query);
        AggregationOperation vectorSearchStage = ctx -> new Document("$vectorSearch",
                new Document("index", VECTOR_INDEX)
                        .append("path", "embedding")
                        .append("queryVector", queryVector)
                        .append("numCandidates", limit * 10)
                        .append("limit", limit));
        AggregationResults<Product> results = mongoTemplate.aggregate(
                Aggregation.newAggregation(vectorSearchStage), "products", Product.class);
        return results.getMappedResults().stream()
                .peek(this::resolveImageUrls)
                .toList();
    }

    public int reindexAllProducts() {
        int count = 0;
        for (Product product : productRepository.findAll()) {
            try {
                attachEmbedding(product);
                productRepository.save(product);
                count++;
            } catch (Exception e) {
                log.error("Failed to embed product {}: {}", product.getId(), e.getMessage());
            }
        }
        log.info("Reindexed {} products", count);
        return count;
    }

    public List<String> getDistinctBrands() {
        return mongoTemplate.query(Product.class).distinct("brand").as(String.class).all();
    }

    public List<String> getDistinctCategories() {
        return mongoTemplate.query(Product.class).distinct("categories").as(String.class).all();
    }

    private void attachEmbedding(Product product) {
        String text = embeddingService.buildProductText(
                product.getName(), product.getDescription(), product.getCategories(), product.getBrand());
        product.setEmbedding(embeddingService.embed(text));
    }

    private void resolveImageUrls(Product product) {
        if (product.getImageUrls() != null) {
            product.setImageUrls(product.getImageUrls().stream()
                    .map(key -> s3Service.generatePresignedUrl(s3Buckets.getEcomm(), key))
                    .toList());
        }
    }
}
