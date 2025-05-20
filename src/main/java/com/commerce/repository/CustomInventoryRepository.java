package com.commerce.repository;

import com.commerce.model.Inventory;

import java.util.List;

public interface CustomInventoryRepository {
    List<Inventory> findInventoryByProductId(String productId);

}
