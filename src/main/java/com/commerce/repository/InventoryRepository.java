package com.commerce.repository;

import com.commerce.model.Inventory;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface InventoryRepository extends MongoRepository<Inventory, String>, CustomInventoryRepository {
}
