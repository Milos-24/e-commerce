package com.commerce.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(collection = "products")
public class Product {
    @Id
    private String id;
    private String name;
    private String description;
    private double discount;
    private double price;
    private Map<String, Object> attributes;
    //   Clothes -> {"size": "M", "color": "Black", "material": "Polyester"}
    //   Supplement -> {"weight": "500g", "flavor": "Vanilla", "ingredients": ["Whey", "BCAA"]}
    //   Food -> {"calories": 250, "protein": "20g", "organic": true}
    private List<String> categories; // References Category collection
    private String brand;            // References Brand collection
    private List<Review> reviews;
    private List<String> imageUrls; // S3 object keys

}