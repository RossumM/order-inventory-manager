package dev.merijn.orderinventory.service;

import dev.merijn.orderinventory.domain.MovementReason;
import dev.merijn.orderinventory.domain.Product;
import dev.merijn.orderinventory.domain.StockMovement;
import dev.merijn.orderinventory.repository.ProductRepository;
import dev.merijn.orderinventory.repository.StockMovementRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * Records stock changes: appends to the {@link StockMovement} ledger and adjusts the
 * {@link Product} quantity-on-hand cache. Constructor-injected {@code final} collaborators.
 *
 * <p>No {@code @Transactional} yet — the two-step write below is deliberately non-atomic until the
 * Transactions subgoal, where an interrupted write is shown diverging the ledger from the counter (I6).
 */
@Service
public class StockService {

    private final ProductRepository products;
    private final StockMovementRepository movements;

    public StockService(final ProductRepository products, final StockMovementRepository movements) {
        this.products = products;
        this.movements = movements;
    }

    public StockMovement recordMovement(final String sku, final int change, final MovementReason reason) {
        Product product = products.findBySku(sku)
                .orElseThrow(() -> new IllegalArgumentException("no product with sku " + sku));

        StockMovement movement = new StockMovement(product, change, reason, Instant.now());
        product.adjustStock(change); // I1 (never negative) guard lives in the entity

        movements.save(movement);    // step 1 — append to the ledger  (its own transaction)
        products.save(product);      // step 2 — persist the counter   (its own transaction)
        return movement;             // step 2 failing after step 1 commits is the I6 drift case
    }

    public int currentStock(final String sku) {
        return products.findBySku(sku)
                .orElseThrow(() -> new IllegalArgumentException("no product with sku " + sku))
                .getQuantityOnHand();
    }
}
