package com.commerce.service;

import com.commerce.model.Product;
import com.commerce.repository.ProductRepository;
import com.commerce.s3.S3Buckets;
import com.commerce.s3.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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

    // Must match the Atlas Vector Search index name you create in the Atlas UI
    private static final String VECTOR_INDEX = "product_vector_index";

    public List<Product> getAllProducts() {
        return productRepository.findAll().stream()
                .peek(product -> {
                    if (product.getImageUrls() != null) {
                        product.setImageUrls(product.getImageUrls().stream()
                                .map(key -> s3Service.generatePresignedUrl(s3Buckets.getEcomm(), key))
                                .toList());
                    }
                })
                .toList();
    }

    public Optional<Product> getProductById(String id) {
        Product product = new Product();
        if (productRepository.findById(id).isPresent()) {
            product = productRepository.findById(id).get();
        }
        product.setImageUrls(product.getImageUrls()
                .stream()
                .map(key -> s3Service.generatePresignedUrl(s3Buckets.getEcomm(), key))
                .toList());

        return Optional.of(product);
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
                .peek(product -> {
                    if (product.getImageUrls() != null) {
                        product.setImageUrls(product.getImageUrls().stream()
                                .map(key -> s3Service.generatePresignedUrl(s3Buckets.getEcomm(), key))
                                .toList());
                    }
                })
                .toList();
    }

    public List<Product> getProductsByCategory(String category) {
        return productRepository.findProductsByCategory(category).stream()
                .peek(product -> {
                    if (product.getImageUrls() != null) {
                        product.setImageUrls(product.getImageUrls().stream()
                                .map(key -> s3Service.generatePresignedUrl(s3Buckets.getEcomm(), key))
                                .toList());
                    }
                })
                .toList();
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
            List<String> imageUrls = new ArrayList<>();
            saved.setImageUrls(imageUrls);

            // 2. Create a unique key for S3
            String key = "images/" + saved.getId() + "/" + UUID.randomUUID();

            // 3. Upload image to S3
            s3Service.putObject(s3Buckets.getEcomm(), key, file.getBytes());

            // 4. Generate and attach embedding
            saved.getImageUrls().add(key);
            attachEmbedding(saved);

            return productRepository.save(saved);
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload image", e);
        }
    }

    /**
     * Semantic vector search using MongoDB Atlas $vectorSearch.
     * Converts the query text to an embedding via OpenAI, then runs the
     * Atlas aggregation pipeline to return the top-N most similar products.
     *
     * @param query  natural-language search string (e.g. "high protein vanilla supplement")
     * @param limit  max results to return (default 10)
     */
    public List<Product> vectorSearch(String query, int limit) {
        List<Double> queryVector = embeddingService.embed(query);

        AggregationOperation vectorSearchStage = ctx -> new Document("$vectorSearch",
                new Document("index", VECTOR_INDEX)
                        .append("path", "embedding")
                        .append("queryVector", queryVector)
                        .append("numCandidates", limit * 10)  // wider candidate pool → better recall
                        .append("limit", limit)
        );

        Aggregation aggregation = Aggregation.newAggregation(vectorSearchStage);
        AggregationResults<Product> results = mongoTemplate.aggregate(aggregation, "products", Product.class);

        return results.getMappedResults().stream()
                .peek(product -> {
                    if (product.getImageUrls() != null) {
                        product.setImageUrls(product.getImageUrls().stream()
                                .map(key -> s3Service.generatePresignedUrl(s3Buckets.getEcomm(), key))
                                .toList());
                    }
                })
                .toList();
    }

    /**
     * Re-generates embeddings for every product in the database.
     * Call this once after setting up Atlas Vector Search to backfill existing products,
     * or whenever you change the embedding model.
     */
    public int reindexAllProducts() {
        List<Product> all = productRepository.findAll();
        int count = 0;
        for (Product product : all) {
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
        return mongoTemplate.query(Product.class)
                .distinct("brand")
                .as(String.class)
                .all();
    }

    public List<String> getDistinctCategories() {
        return mongoTemplate.query(Product.class)
                .distinct("categories")
                .as(String.class)
                .all();
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private void attachEmbedding(Product product) {
        String text = embeddingService.buildProductText(
                product.getName(),
                product.getDescription(),
                product.getCategories(),
                product.getBrand()
        );
        if (!text.isBlank()) {
            product.setEmbedding(embeddingService.embed(text));
        }
    }
}