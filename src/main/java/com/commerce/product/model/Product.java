package com.commerce.product.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.Map;

@Data @AllArgsConstructor @NoArgsConstructor @Builder
@Document(collection = "products")
@CompoundIndexes({
        @CompoundIndex(name = "price_idx",      def = "{'price': 1}"),
        @CompoundIndex(name = "brand_idx",      def = "{'brand': 1}"),
        @CompoundIndex(name = "categories_idx", def = "{'categories': 1}"),
        @CompoundIndex(name = "name_idx",       def = "{'name': 1}")
})
public class Product {
    @Id private String id;
    private String name;
    private String description;
    private double discount;
    private boolean discountEnabled;
    private double price;
    private Map<String, Object> attributes;
    private List<String> categories;
    private String brand;
    private List<Review> reviews;
    private List<String> imageUrls;

    @JsonIgnore
    private List<Double> embedding;
}
