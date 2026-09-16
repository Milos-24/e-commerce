package com.commerce.product.controller;

import com.commerce.product.model.Product;
import com.commerce.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public Page<Product> getProducts(
            @RequestParam(defaultValue = "0")    int page,
            @RequestParam(defaultValue = "20")   int size,
            @RequestParam(defaultValue = "name") String sortField,
            @RequestParam(defaultValue = "asc")  String sortDir,
            @RequestParam(required = false)      String category,
            @RequestParam(required = false)      String brand,
            @RequestParam(required = false)      Double minPrice,
            @RequestParam(required = false)      Double maxPrice) {
        return productService.getProducts(page, size, sortField, sortDir, category, brand, minPrice, maxPrice);
    }

    @GetMapping("/product/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable String id) {
        return productService.getProductById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/price-range")
    public List<Product> getProductsByPriceRange(@RequestParam double minPrice, @RequestParam double maxPrice) {
        return productService.getProductsByPriceRange(minPrice, maxPrice);
    }

    @GetMapping("/category/{category}")
    public List<Product> getProductsByCategory(@PathVariable String category) {
        return productService.getProductsByCategory(category);
    }

    @PostMapping("/{productId}")
    public ResponseEntity<Void> uploadImage(@PathVariable String productId,
                                            @RequestParam("file") MultipartFile file) {
        productService.uploadImage(productId, file);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{productId}")
    public ResponseEntity<Product> getProductWithImage(@PathVariable String productId) {
        return productService.getProductById(productId).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Product createProduct(@RequestParam("product") String productJson,
                                 @RequestParam("file") MultipartFile file) throws Exception {
        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        Product product = mapper.readValue(productJson, Product.class);
        return productService.createProductWithImage(product, file);
    }

    @GetMapping("/categories/distinct")
    public List<String> getDistinctCategories() { return productService.getDistinctCategories(); }

    @CrossOrigin
    @GetMapping("/search")
    public List<Product> vectorSearch(@RequestParam String q,
                                      @RequestParam(defaultValue = "10") int limit) {
        return productService.vectorSearch(q, limit);
    }

    @PostMapping("/reindex")
    public ResponseEntity<String> reindex() {
        int count = productService.reindexAllProducts();
        return ResponseEntity.ok("Reindexed " + count + " products");
    }
}
