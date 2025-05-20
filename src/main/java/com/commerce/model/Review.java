package com.commerce.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Review {
    private String customerId; // References Customer collection
    private String comment;
    private int rating;

}

