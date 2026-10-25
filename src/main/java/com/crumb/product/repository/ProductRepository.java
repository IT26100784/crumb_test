package com.crumb.product.repository;

import com.crumb.product.entity.BakeryProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * ProductRepository - Data access interface for BakeryProduct entities.
 * 
 * WHY: Provides query methods for retrieving catalog items, filtered by availability,
 * keyword search, and product category (Cake, Pastry, Bread).
 */
@Repository
public interface ProductRepository extends JpaRepository<BakeryProduct, Long> {

    /**
     * Finds all currently available products for customer store browsing.
     */
    List<BakeryProduct> findByAvailableTrue();

    /**
     * Searches products where name or description matches keyword.
     */
    List<BakeryProduct> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String name, String desc);

    /**
     * Flexible catalog search supporting optional keyword and category filter.
     */
    @Query("SELECT p FROM BakeryProduct p WHERE " +
           "(:keyword IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "ORDER BY p.id DESC")
    List<BakeryProduct> searchProducts(@Param("keyword") String keyword);
}
