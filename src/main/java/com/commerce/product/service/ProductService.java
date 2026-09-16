package com.commerce.product.service;

import com.commerce.catalog.category.model.AttributeDefinition;
import com.commerce.catalog.category.model.Category;
import com.commerce.catalog.category.service.CategoryService;
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
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import org.springframework.data.domain.Page;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;
    private final S3Service s3Service;
    private final S3Buckets s3Buckets;
    private final MongoTemplate mongoTemplate;
    private final EmbeddingService embeddingService;
    private final CategoryService categoryService;

    private static final String VECTOR_INDEX = "products";

    public Page<Product> getProducts(int page, int size, String sortField, String sortDir,
                                      String category, String brand, Double minPrice, Double maxPrice) {
        return productRepository.findProductsFiltered(page, size, sortField, sortDir, category, brand, minPrice, maxPrice)
                .map(p -> { resolveImageUrls(p); return p; });
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
        validateProductAttributes(product);
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
            validateProductAttributes(product);
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

    private void validateProductAttributes(Product product) {
        List<String> categoryIds = product.getCategories();
        if (categoryIds == null || categoryIds.isEmpty()) return;

        List<Category> categories = categoryService.findAllByIds(categoryIds);
        Set<String> foundIds = categories.stream().map(Category::getId).collect(Collectors.toSet());
        List<String> unknown = categoryIds.stream().filter(id -> !foundIds.contains(id)).toList();
        if (!unknown.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Unknown category slugs: " + String.join(", ", unknown));
        }

        if (product.getAttributes() == null || product.getAttributes().isEmpty()) return;

        Map<String, AttributeDefinition> defined = categories.stream()
                .filter(c -> "product".equals(c.getKind()))
                .filter(c -> c.getAttributes() != null)
                .flatMap(c -> c.getAttributes().stream())
                .collect(Collectors.toMap(AttributeDefinition::getKey, a -> a, (a, b) -> a));

        for (Map.Entry<String, Object> entry : product.getAttributes().entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (!defined.containsKey(key)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Unknown attribute key '" + key + "' for this product's categories");
            }
            if (value == null) continue;
            AttributeDefinition def = defined.get(key);
            switch (def.getType()) {
                case "number"  -> { if (!(value instanceof Number))
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Attribute '" + key + "' must be a number"); }
                case "boolean" -> { if (!(value instanceof Boolean))
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Attribute '" + key + "' must be a boolean"); }
                case "enum"    -> { if (def.getOptions() == null || !def.getOptions().contains(value.toString()))
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Attribute '" + key + "' must be one of: " + def.getOptions()); }
            }
        }
    }

    private void attachEmbedding(Product product) {
        String text = embeddingService.buildProductText(
                product.getName(), product.getDescription(), product.getCategories(), product.getBrand());
        product.setEmbedding(embeddingService.embed(text));
    }

    public Product setDiscount(String id, double percentage) {
        if (percentage < 0 || percentage > 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "percentage must be between 0 and 100");
        }
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found: " + id));
        product.setDiscount(percentage);
        product.setDiscountEnabled(percentage > 0);
        Product saved = productRepository.save(product);
        resolveImageUrls(saved);
        return saved;
    }

    public long setDiscountByCategory(String category, double percentage) {
        if (percentage < 0 || percentage > 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "percentage must be between 0 and 100");
        }
        Query query = new Query(Criteria.where("categories").is(category));
        Update update = new Update()
                .set("discount", percentage)
                .set("discountEnabled", percentage > 0);
        return mongoTemplate.updateMulti(query, update, Product.class).getModifiedCount();
    }

    private void resolveImageUrls(Product product) {
        if (product.getImageUrls() != null) {
            product.setImageUrls(product.getImageUrls().stream()
                    .map(key -> s3Service.generatePresignedUrl(s3Buckets.getEcomm(), key))
                    .toList());
        }
        if (!product.isDiscountEnabled()) {
            product.setDiscount(0);
        }
    }
}
