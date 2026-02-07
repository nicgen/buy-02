package com.buy01.productservice.service;

import com.buy01.productservice.model.Product;
import com.buy01.productservice.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private Product testProduct;

    @BeforeEach
    void setUp() {
        testProduct = new Product();
        testProduct.setId("prod1");
        testProduct.setName("Test Product");
        testProduct.setPrice(100.00);
        testProduct.setSellerId("seller1");
    }

    @Test
    void createProduct_shouldSetSellerIdAndSave() {
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        Product result = productService.createProduct(testProduct, "seller1");

        assertNotNull(result);
        assertEquals("seller1", result.getSellerId());
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void updateProduct_shouldUpdate_whenSellerIsOwner() {
        when(productRepository.findById("prod1")).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        Product updateRequest = new Product();
        updateRequest.setName("Updated Name");
        updateRequest.setPrice(150.00);

        Product result = productService.updateProduct("prod1", updateRequest, "seller1");

        assertEquals("Updated Name", result.getName());
        assertEquals(150.00, result.getPrice());
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void updateProduct_shouldThrowException_whenSellerIsNotOwner() {
        when(productRepository.findById("prod1")).thenReturn(Optional.of(testProduct));

        Product updateRequest = new Product();
        updateRequest.setName("Hacked Name");

        assertThrows(RuntimeException.class, () -> {
            productService.updateProduct("prod1", updateRequest, "hacker");
        });

        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void deleteProduct_shouldDelete_whenSellerIsOwner() {
        when(productRepository.findById("prod1")).thenReturn(Optional.of(testProduct));

        productService.deleteProduct("prod1", "seller1");

        verify(productRepository).deleteById("prod1");
    }

    @Test
    void deleteProduct_shouldThrowException_whenSellerIsNotOwner() {
        when(productRepository.findById("prod1")).thenReturn(Optional.of(testProduct));

        assertThrows(RuntimeException.class, () -> {
            productService.deleteProduct("prod1", "hacker");
        });

        verify(productRepository, never()).deleteById(anyString());
    }
}
