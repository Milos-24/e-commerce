package com.commerce.inventory.repository;

import com.commerce.inventory.model.Inventory;

import java.util.List;

public interface CustomInventoryRepository {
    List<Inventory> findInventoryByProductId(String productId);
}
