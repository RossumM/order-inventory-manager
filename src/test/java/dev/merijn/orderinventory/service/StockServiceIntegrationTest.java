package dev.merijn.orderinventory.service;

import dev.merijn.orderinventory.domain.MovementReason;
import dev.merijn.orderinventory.domain.Money;
import dev.merijn.orderinventory.domain.Product;
import dev.merijn.orderinventory.repository.ProductRepository;
import dev.merijn.orderinventory.repository.StockMovementRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.Currency;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Runs StockService with the real repositories against the in-memory database.
 *
 * @author Merijn van Rossum
 */
@SpringBootTest
class StockServiceIntegrationTest {

    @Autowired
    private StockService service;
    @Autowired
    private ProductRepository products;
    @Autowired
    private StockMovementRepository movements;

    @Test
    void movementsArePersistedAndTheCounterFollows() {
        products.save(new Product("SKU-INT", "Widget",
                new Money(new BigDecimal("2.00"), Currency.getInstance("EUR"))));

        service.recordMovement("SKU-INT", 40, MovementReason.DELIVERY);
        service.recordMovement("SKU-INT", -3, MovementReason.SALE);

        assertThat(service.currentStock("SKU-INT")).isEqualTo(37);
        assertThat(movements.findAll()).hasSize(2);
    }
}
