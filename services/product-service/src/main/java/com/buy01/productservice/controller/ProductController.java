package com.buy01.productservice.controller;

import com.buy01.productservice.model.Product;
import com.buy01.productservice.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @GetMapping("/seller")
    public ResponseEntity<List<Product>> getSellerProducts(Principal principal) {
        return ResponseEntity.ok(productService.getProductsBySeller(principal.getName()));
    }

    @GetMapping("/search")
    public ResponseEntity<List<Product>> searchProducts(@RequestParam String query) {
        return ResponseEntity.ok(productService.searchProducts(query));
    }

    @GetMapping("/filter")
    public ResponseEntity<List<Product>> filterProducts(
            @RequestParam(required = false) java.math.BigDecimal minPrice,
            @RequestParam(required = false) java.math.BigDecimal maxPrice) {

        if (minPrice == null)
            minPrice = java.math.BigDecimal.ZERO;
        if (maxPrice == null)
            maxPrice = new java.math.BigDecimal("1000000"); // Arbitrary large number

        return ResponseEntity.ok(productService.filterProducts(minPrice, maxPrice));
    }

    @PostMapping
    public ResponseEntity<Product> createProduct(@RequestBody Product product, Principal principal) {
        return ResponseEntity.ok(productService.createProduct(product, principal.getName()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable String id, @RequestBody Product product,
            Principal principal) {
        return ResponseEntity.ok(productService.updateProduct(id, product, principal.getName()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable String id, Principal principal) {
        productService.deleteProduct(id, principal.getName());
        return ResponseEntity.noContent().build();
    }
}
