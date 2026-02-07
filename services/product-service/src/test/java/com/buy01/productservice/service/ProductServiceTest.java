package com.buy01.productservice.service;

import com.buy01.productservice.model.Product;
import com.buy01.productservice.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

    private static final String PRODUCT_ID = "prod1";
    private static final String SELLER_ID = "seller1";

    @BeforeEach
    void setUp() {
        testProduct = new Product();
        testProduct.setId(PRODUCT_ID);
        testProduct.setName("Test Product");
        testProduct.setPrice(100.00);
        testProduct.setSellerId(SELLER_ID);
    }

    @Test
    void createProductShouldSetSellerIdAndSave() {
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        Product result = productService.createProduct(testProduct, SELLER_ID);

        assertNotNull(result);
        assertEquals(SELLER_ID, result.getSellerId());
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void updateProductShouldUpdateWhenSellerIsOwner() {
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        Product updateRequest = new Product();
        updateRequest.setName("Updated Name");
        updateRequest.setPrice(150.00);

        Product result = productService.updateProduct(PRODUCT_ID, updateRequest, SELLER_ID);

        assertEquals("Updated Name", result.getName());
        assertEquals(150.00, result.getPrice());
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void updateProductShouldThrowExceptionWhenSellerIsNotOwner() {
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(testProduct));

        Product updateRequest = new Product();
        updateRequest.setName("Hacked Name");

        assertThrows(RuntimeException.class, () -> {
            productService.updateProduct(PRODUCT_ID, updateRequest, "hacker");
        });

        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void deleteProductShouldDeleteWhenSellerIsOwner() {
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(testProduct));

        productService.deleteProduct(PRODUCT_ID, SELLER_ID);

        verify(productRepository).deleteById(PRODUCT_ID);
    }

    @Test
    void deleteProductShouldThrowExceptionWhenSellerIsNotOwner() {
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(testProduct));

        assertThrows(RuntimeException.class, () -> {
            productService.deleteProduct(PRODUCT_ID, "hacker");
        });

        verify(productRepository, never()).deleteById(anyString());
    }
}
