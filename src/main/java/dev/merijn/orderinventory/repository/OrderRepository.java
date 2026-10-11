package dev.merijn.orderinventory.repository;

import dev.merijn.orderinventory.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author Merijn van Rossum
 */
public interface OrderRepository extends JpaRepository<Order, Long> {
}
