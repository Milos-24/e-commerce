package com.commerce.inventory.service;

import com.commerce.inventory.model.Inventory;
import com.commerce.inventory.repository.InventoryRepository;
import com.commerce.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;
    private final MongoTemplate mongoTemplate;

    public List<Inventory> getAllInventories() { return inventoryRepository.findAll(); }
    public Optional<Inventory> getInventoryById(String id) { return inventoryRepository.findById(id); }
    public Inventory saveInventory(Inventory inventory) { return inventoryRepository.save(inventory); }
    public void deleteInventory(String id) { inventoryRepository.deleteById(id); }
    public List<Inventory> getInventoryByProductId(String productId) {
        return inventoryRepository.findInventoryByProductId(productId);
    }

    /**
     * Atomically decrements stock if sufficient quantity is available.
     * Returns true on success, false if stock is insufficient or no inventory record exists.
     */
    public boolean decrementStock(String productId, int quantity) {
        Query q = Query.query(Criteria.where("productId").is(productId).and("stock").gte(quantity));
        Inventory before = mongoTemplate.findAndModify(
                q, new Update().inc("stock", -quantity),
                FindAndModifyOptions.options().returnNew(false),
                Inventory.class);
        return before != null;
    }

    public void incrementStock(String productId, int quantity) {
        mongoTemplate.findAndModify(
                Query.query(Criteria.where("productId").is(productId)),
                new Update().inc("stock", quantity),
                Inventory.class);
    }

    public int seedMissingInventory(int defaultStock) {
        Set<String> existing = inventoryRepository.findAll().stream()
                .map(Inventory::getProductId).collect(Collectors.toSet());
        List<Inventory> toCreate = productRepository.findAll().stream()
                .filter(p -> !existing.contains(p.getId()))
                .map(p -> Inventory.builder().productId(p.getId()).stock(defaultStock).build())
                .toList();
        inventoryRepository.saveAll(toCreate);
        return toCreate.size();
    }
}
