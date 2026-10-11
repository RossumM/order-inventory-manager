package dev.merijn.orderinventory.repository;

import dev.merijn.orderinventory.domain.StockMovement;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author Merijn van Rossum
 */
public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {
}
