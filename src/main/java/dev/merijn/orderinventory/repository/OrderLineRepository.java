package dev.merijn.orderinventory.repository;

import dev.merijn.orderinventory.domain.OrderLine;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author Merijn van Rossum
 */
public interface OrderLineRepository extends JpaRepository<OrderLine, Long> {
}
