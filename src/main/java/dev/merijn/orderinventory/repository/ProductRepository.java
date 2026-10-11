package dev.merijn.orderinventory.repository;

import dev.merijn.orderinventory.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * {@code JpaRepository} gives CRUD + paging; Spring Data generates the implementation
 * (a proxy over {@code SimpleJpaRepository}) at startup and registers it as a bean.
 */
public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findBySku(String sku);
}
