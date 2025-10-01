package com.commerce.controller;

import com.commerce.model.Product;
import com.commerce.service.ProductService;
import lombok.RequiredArgsConstructor;
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
    @CrossOrigin
    public List<Product> getAllProducts() {
        return productService.getAllProducts();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable String id) {
        return productService.getProductById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable String id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/price-range")
    public List<Product> getProductsByPriceRange(@RequestParam double minPrice, @RequestParam double maxPrice) {
        return productService.getProductsByPriceRange(minPrice, maxPrice);
    }

    @GetMapping("/category")
    public List<Product> getProductsByCategory(@RequestParam String category) {
        return productService.getProductsByCategory(category);
    }

    @PostMapping("/{productId}")
    public ResponseEntity<String> uploadImage(
            @PathVariable String productId,
            @RequestParam("file") MultipartFile file) {
        productService.uploadImage(productId, file);
        return ResponseEntity.ok("success");
    }

    @GetMapping("/{productId}")
    public ResponseEntity<byte[]> getImage(
            @PathVariable String productId) {
        byte[] productImage = productService.getImage(productId);
        return ResponseEntity.ok(productImage);
    }


    @PostMapping
    public ResponseEntity<Product> createProduct(
            @RequestPart("product") Product product,
            @RequestParam("file") MultipartFile file
    ) {
        Product createdProduct = productService.createProductWithImage(product, file);
        return ResponseEntity.ok(createdProduct);
    }

    @CrossOrigin
    @GetMapping("/categories/distinct")
    public List<String> getDistinctCategories() {
        return productService.getDistinctCategories();
    }

}
