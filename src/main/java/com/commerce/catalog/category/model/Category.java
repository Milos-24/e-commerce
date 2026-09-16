package com.commerce.catalog.category.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data @AllArgsConstructor @NoArgsConstructor @Builder
@Document(collection = "categories")
public class Category {
    @Id private String id;          // slug, e.g. "clothing", "pre-workout"
    private String name;
    private String kind;            // "audience" | "product"
    private List<AttributeDefinition> attributes;
}
