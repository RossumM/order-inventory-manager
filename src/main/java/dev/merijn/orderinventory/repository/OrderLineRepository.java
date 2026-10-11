package dev.merijn.orderinventory.repository;

import dev.merijn.orderinventory.domain.OrderLine;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderLineRepository extends JpaRepository<OrderLine, Long> {
}
