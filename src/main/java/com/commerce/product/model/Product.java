package com.commerce.product.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.Map;

@Data @AllArgsConstructor @NoArgsConstructor @Builder
@Document(collection = "products")
public class Product {
    @Id private String id;
    private String name;
    private String description;
    private double discount;
    private double price;
    private Map<String, Object> attributes;
    private List<String> categories;
    private String brand;
    private List<Review> reviews;
    private List<String> imageUrls;

    @JsonIgnore
    private List<Double> embedding;
}
