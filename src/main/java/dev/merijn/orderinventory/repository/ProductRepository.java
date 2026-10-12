package dev.merijn.orderinventory.repository;

import dev.merijn.orderinventory.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Spring Data generates the implementation of this interface at startup, so only the extra queries need to be
 * declared.
 *
 * @author Merijn van Rossum
 */
public interface ProductRepository extends JpaRepository<Product, Long> {

    /** Finds a product by its SKU. */
    Optional<Product> findBySku(String sku);
}
