package com.commerce.service;

import com.commerce.model.Inventory;
import com.commerce.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    public List<Inventory> getAllInventories() {
        return inventoryRepository.findAll();
    }

    public Optional<Inventory> getInventoryById(String id) {
        return inventoryRepository.findById(id);
    }

    public Inventory saveInventory(Inventory inventory) {
        return inventoryRepository.save(inventory);
    }

    public void deleteInventory(String id) {
        inventoryRepository.deleteById(id);
    }

    // Custom methods
    public List<Inventory> getInventoryByProductId(String productId) {
        return inventoryRepository.findInventoryByProductId(productId);
    }
}
