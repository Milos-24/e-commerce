package com.commerce.inventory.controller;

import com.commerce.inventory.model.Inventory;
import com.commerce.inventory.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/inventories")
public class InventoryController {
    private final InventoryService inventoryService;

    @GetMapping
    public List<Inventory> getAllInventories() { return inventoryService.getAllInventories(); }

    @GetMapping("/{id}")
    public ResponseEntity<Inventory> getInventoryById(@PathVariable String id) {
        return inventoryService.getInventoryById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Inventory createInventory(@RequestBody Inventory inventory) { return inventoryService.saveInventory(inventory); }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInventory(@PathVariable String id) {
        inventoryService.deleteInventory(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/product/{productId}")
    public List<Inventory> getInventoryByProductId(@PathVariable String productId) {
        return inventoryService.getInventoryByProductId(productId);
    }

    @PostMapping("/seed")
    public ResponseEntity<String> seedInventory(@RequestParam(defaultValue = "100") int defaultStock) {
        int count = inventoryService.seedMissingInventory(defaultStock);
        return ResponseEntity.ok("Created inventory records for " + count + " products");
    }
}
