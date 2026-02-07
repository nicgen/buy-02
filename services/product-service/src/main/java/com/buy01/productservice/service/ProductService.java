package com.buy01.productservice.service;

import com.buy01.productservice.model.Product;
import com.buy01.productservice.repository.ProductRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public List<Product> getProductsBySeller(String sellerId) {
        return productRepository.findBySellerId(sellerId);
    }

    public List<Product> searchProducts(String query) {
        return productRepository.findByNameContainingIgnoreCase(query);
    }

    public List<Product> filterProducts(String query, java.math.BigDecimal minPrice, java.math.BigDecimal maxPrice) {
        if (query != null && !query.trim().isEmpty()) {
            return productRepository.findByNameAndPriceRange(query, minPrice.doubleValue(), maxPrice.doubleValue());
        }
        return productRepository.findByPriceRange(minPrice.doubleValue(), maxPrice.doubleValue());
    }

    public Product createProduct(Product product, String sellerId) {
        product.setSellerId(sellerId);
        return productRepository.save(product);
    }

    public Product updateProduct(String id, Product product, String sellerId) {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));

        if (!existingProduct.getSellerId().equals(sellerId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Unauthorized to update this product");
        }

        existingProduct.setName(product.getName());
        existingProduct.setDescription(product.getDescription());
        existingProduct.setPrice(product.getPrice());
        existingProduct.setImageUrls(product.getImageUrls());

        return productRepository.save(existingProduct);
    }

    public void deleteProduct(String id, String sellerId) {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));

        if (!existingProduct.getSellerId().equals(sellerId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Unauthorized to delete this product");
        }

        productRepository.deleteById(id);
    }
}
