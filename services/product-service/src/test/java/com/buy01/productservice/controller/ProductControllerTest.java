package com.buy01.productservice.controller;

import com.buy01.productservice.model.Product;
import com.buy01.productservice.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @MockBean
    private com.buy01.productservice.security.JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    private Product product;

    @BeforeEach
    void setUp() {
        product = new Product();
        product.setId("1");
        product.setName("Test Product");
        product.setPrice(100.00);
        product.setSellerId("seller");
    }

    @Test
    @WithMockUser
    void getAllProductsShouldReturnList() throws Exception {
        List<Product> products = Arrays.asList(product);
        given(productService.getAllProducts()).willReturn(products);

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Test Product"));
    }

    @Test
    @WithMockUser(username = "seller")
    void getSellerProductsShouldReturnList() throws Exception {
        List<Product> products = Arrays.asList(product);
        given(productService.getProductsBySeller("seller")).willReturn(products);

        mockMvc.perform(get("/api/products/seller"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].sellerId").value("seller"));
    }

    @Test
    @WithMockUser
    void searchProductsShouldReturnList() throws Exception {
        List<Product> products = Arrays.asList(product);
        given(productService.searchProducts("Test")).willReturn(products);

        mockMvc.perform(get("/api/products/search").param("query", "Test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Test Product"));
    }

    @Test
    @WithMockUser
    void filterProductsShouldReturnList() throws Exception {
        List<Product> products = Arrays.asList(product);
        given(productService.filterProducts(any(BigDecimal.class), any(BigDecimal.class)))
                .willReturn(products);

        mockMvc.perform(get("/api/products/filter")
                .param("minPrice", "10")
                .param("maxPrice", "200"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].price").value(100.00));
    }

    @Test
    @WithMockUser(username = "seller")
    void createProductShouldReturnProduct() throws Exception {
        given(productService.createProduct(any(Product.class), eq("seller"))).willReturn(product);

        mockMvc.perform(post("/api/products")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(product)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Product"));
    }

    @Test
    @WithMockUser(username = "seller")
    void updateProductShouldReturnProduct() throws Exception {
        given(productService.updateProduct(eq("1"), any(Product.class), eq("seller"))).willReturn(product);

        mockMvc.perform(put("/api/products/{id}", "1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(product)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Product"));
    }

    @Test
    @WithMockUser(username = "seller")
    void deleteProductShouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/products/{id}", "1")
                .with(csrf()))
                .andExpect(status().isNoContent());

        verify(productService).deleteProduct("1", "seller");
    }
}
