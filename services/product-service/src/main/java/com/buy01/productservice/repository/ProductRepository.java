package com.buy01.productservice.repository;

import com.buy01.productservice.model.Product;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import java.util.List;

public interface ProductRepository extends MongoRepository<Product, String> {
    List<Product> findBySellerId(String sellerId);

    List<Product> findByNameContainingIgnoreCase(String name);

    @Query("{ 'price': { $gte: ?0, $lte: ?1 } }")
    List<Product> findByPriceRange(double minPrice, double maxPrice);

    @Query("{ 'name': { $regex: ?0, $options: 'i' }, 'price': { $gte: ?1, $lte: ?2 } }")
    List<Product> findByNameAndPriceRange(String name, double minPrice, double maxPrice);
}
