package dev.merijn.orderinventory.repository;

import dev.merijn.orderinventory.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
