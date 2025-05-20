package com.commerce.controller;

import com.commerce.model.Brand;
import com.commerce.service.BrandService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/brands")
public class BrandController {
    private final BrandService brandService;

    @GetMapping
    public List<Brand> getAllBrands() {
        return brandService.getAllBrands();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Brand> getBrandById(@PathVariable String id) {
        return brandService.getBrandById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Brand createBrand(@RequestBody Brand brand) {
        return brandService.saveBrand(brand);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBrand(@PathVariable String id) {
        brandService.deleteBrand(id);
        return ResponseEntity.noContent().build();
    }
}
