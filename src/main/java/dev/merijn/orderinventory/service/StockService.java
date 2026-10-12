package dev.merijn.orderinventory.service;

import dev.merijn.orderinventory.domain.MovementReason;
import dev.merijn.orderinventory.domain.Product;
import dev.merijn.orderinventory.domain.StockMovement;
import dev.merijn.orderinventory.repository.ProductRepository;
import dev.merijn.orderinventory.repository.StockMovementRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * Records stock changes: adds a movement to the history and updates the quantity on hand of the product.
 *
 * There is no transaction around this yet, so the two writes can fail separately. That is on purpose for now;
 * transactions come later.
 *
 * @author Merijn van Rossum
 */
@Service
public class StockService {

    private final ProductRepository products;
    private final StockMovementRepository movements;

    public StockService(final ProductRepository products, final StockMovementRepository movements) {
        this.products = products;
        this.movements = movements;
    }

    /**
     * Records a stock movement for the product with this SKU and updates its quantity on hand.
     *
     * @throws IllegalArgumentException if no product has this SKU
     */
    public StockMovement recordMovement(final String sku, final int change, final MovementReason reason) {
        Product product = products.findBySku(sku)
                .orElseThrow(() -> new IllegalArgumentException("no product with sku " + sku));

        StockMovement movement = new StockMovement(product, change, reason, Instant.now());
        product.adjustStock(change); // the product makes sure the stock never goes negative

        movements.save(movement);    // step 1: save the movement (own transaction)
        products.save(product);      // step 2: save the new quantity (own transaction)
        return movement;             // if step 2 fails after step 1, the history and the quantity no longer match
    }

    /**
     * Returns the quantity on hand for the product with this SKU.
     *
     * @throws IllegalArgumentException if no product has this SKU
     */
    public int currentStock(final String sku) {
        return products.findBySku(sku)
                .orElseThrow(() -> new IllegalArgumentException("no product with sku " + sku))
                .getQuantityOnHand();
    }
}
