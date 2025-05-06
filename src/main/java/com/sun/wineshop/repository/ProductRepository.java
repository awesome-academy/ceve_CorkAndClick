package com.sun.wineshop.repository;

import com.sun.wineshop.model.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query("""
        SELECT DISTINCT p FROM Product p
        JOIN p.categories c
        WHERE (:keyword IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
            AND (:minPrice IS NULL OR p.price >= :minPrice)
            AND (:maxPrice IS NULL OR p.price <= :maxPrice)
            AND (:minAlcohol IS NULL OR p.alcoholPercentage >= :minAlcohol)
            AND (:maxAlcohol IS NULL OR p.alcoholPercentage <= :maxAlcohol)
            AND (:categoryIds IS NULL OR c.id IN :categoryIds)
    """)
    Page<Product> searchProducts(
        @Param("keyword") String keyword,
        @Param("minPrice") Double minPrice,
        @Param("maxPrice") Double maxPrice,
        @Param("minAlcohol") Double minAlcohol,
        @Param("maxAlcohol") Double maxAlcohol,
        @Param("categoryIds") List<Long> categoryIds,
        Pageable pageable
    );
}
