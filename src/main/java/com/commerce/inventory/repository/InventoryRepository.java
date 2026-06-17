package com.commerce.inventory.repository;

import com.commerce.inventory.model.Inventory;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface InventoryRepository extends MongoRepository<Inventory, String>, CustomInventoryRepository {
}
