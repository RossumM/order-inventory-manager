package dev.merijn.orderinventory.domain;

import jakarta.persistence.*;

import java.util.Objects;

/**
 * An article that can be stocked and sold.
 *
 * <p>{@code quantityOnHand} is a fast-read cache; the {@link StockMovement} ledger is the
 * source of truth (invariant I6). This class only enforces the single-threaded guard that
 * the cache never goes negative (I1) — the concurrency-safe version of the decrement is a
 * later mark.
 *
 * @author Merijn van Rossum
 */
@Entity
@Table(name = "PRODUCTS")
public class Product extends AbstractEntity {

    @Column(nullable = false, unique = true)
    private String sku;
    private String name;
    private Money unitPrice;
    private int quantityOnHand;
    private boolean discontinued;

    protected Product() {
        // required by JPA / proxies
    }

    public Product(final String sku, final String name, final Money unitPrice) {
        this.sku = Objects.requireNonNull(sku, "sku");
        this.name = Objects.requireNonNull(name, "name");
        this.unitPrice = Objects.requireNonNull(unitPrice, "unitPrice");
        this.quantityOnHand = 0;
        this.discontinued = false;
    }

    /**
     * Apply a signed change to the running stock cache.
     * Positive = delivery / restock / customer return, negative = sale / correction.
     *
     * @throws IllegalStateException if the result would be negative (I1)
     */
    public void adjustStock(final int delta) {
        int updated = this.quantityOnHand + delta;
        if (updated < 0) {
            throw new IllegalStateException(
                    "stock for %s would go negative: %d %+d".formatted(sku, quantityOnHand, delta));
        }
        this.quantityOnHand = updated;
    }

    /** Change the current selling price. Does not touch any {@link OrderLine} already placed (I4). */
    public void changeUnitPrice(final Money newPrice) {
        this.unitPrice = Objects.requireNonNull(newPrice, "newPrice");
    }

    public void rename(final String newName) {
        this.name = Objects.requireNonNull(newName, "newName");
    }

    public void discontinue() {
        this.discontinued = true;
    }

    public String getSku() {
        return sku;
    }

    public String getName() {
        return name;
    }

    public Money getUnitPrice() {
        return unitPrice;
    }

    public int getQuantityOnHand() {
        return quantityOnHand;
    }

    public boolean isDiscontinued() {
        return discontinued;
    }

    // equals() / hashCode() inherited from AbstractEntity (uuid-based).

    @Override
    public String toString() {
        return "Product{id=%s, sku=%s, name=%s, quantityOnHand=%d, discontinued=%b}"
                .formatted(getId(), sku, name, quantityOnHand, discontinued);
    }
}
