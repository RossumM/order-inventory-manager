package dev.merijn.orderinventory.service;

import dev.merijn.orderinventory.domain.MovementReason;
import dev.merijn.orderinventory.domain.Money;
import dev.merijn.orderinventory.domain.Product;
import dev.merijn.orderinventory.domain.StockMovement;
import dev.merijn.orderinventory.repository.ProductRepository;
import dev.merijn.orderinventory.repository.StockMovementRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Constructor injection means the service is a plain object: {@code new StockService(mock, mock)},
 * no Spring container. This test exercises that.
 */
class StockServiceTest {

    private final ProductRepository products = mock(ProductRepository.class);
    private final StockMovementRepository movements = mock(StockMovementRepository.class);
    private final StockService service = new StockService(products, movements);

    private static Product product() {
        return new Product("SKU-1", "Widget", new Money(new BigDecimal("2.00"), Currency.getInstance("EUR")));
    }

    @Test
    void recordMovementAdjustsCounterAndAppendsToLedger() {
        Product p = product();
        when(products.findBySku("SKU-1")).thenReturn(Optional.of(p));

        StockMovement m = service.recordMovement("SKU-1", 12, MovementReason.DELIVERY);

        assertThat(p.getQuantityOnHand()).isEqualTo(12);
        assertThat(m.getChange()).isEqualTo(12);
        verify(movements).save(m);
        verify(products).save(p);
    }

    @Test
    void unknownSkuThrowsIllegalArgumentException() {
        when(products.findBySku("NOPE")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.recordMovement("NOPE", 1, MovementReason.DELIVERY))
                .isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(movements);
    }

    @Test
    void negativeResultThrowsIllegalStateException() {
        Product p = product(); // quantityOnHand starts at 0
        when(products.findBySku("SKU-1")).thenReturn(Optional.of(p));

        assertThatThrownBy(() -> service.recordMovement("SKU-1", -1, MovementReason.CORRECTION))
                .isInstanceOf(IllegalStateException.class);
        verifyNoInteractions(movements);
    }
}
