package dev.merijn.orderinventory.domain;

import jakarta.persistence.*;

import java.util.Objects;

/**
 * Something that can be stocked and sold.
 *
 * The quantity on hand is a stored running total, so it can be read quickly. The stock movements are the real
 * history; this total follows them. Here we only make sure it never drops below zero. Keeping that safe when
 * several sales happen at the same time comes later.
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

    /** Creates a product with no stock. */
    public Product(final String sku, final String name, final Money unitPrice) {
        this.sku = Objects.requireNonNull(sku, "sku");
        this.name = Objects.requireNonNull(name, "name");
        this.unitPrice = Objects.requireNonNull(unitPrice, "unitPrice");
        this.quantityOnHand = 0;
        this.discontinued = false;
    }

    /**
     * Changes the quantity on hand: positive for a delivery or return, negative for a sale or correction.
     *
     * @throws IllegalStateException if the stock would drop below zero
     */
    public void adjustStock(final int delta) {
        int updated = this.quantityOnHand + delta;
        if (updated < 0) {
            throw new IllegalStateException(
                    "stock for %s would go negative: %d %+d".formatted(sku, quantityOnHand, delta));
        }
        this.quantityOnHand = updated;
    }

    /** Sets a new selling price. Orders that were already placed keep their old price. */
    public void changeUnitPrice(final Money newPrice) {
        this.unitPrice = Objects.requireNonNull(newPrice, "newPrice");
    }

    /** Changes the name. */
    public void rename(final String newName) {
        this.name = Objects.requireNonNull(newName, "newName");
    }

    /** Marks the product as no longer for sale. Its history is kept. */
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

    @Override
    public String toString() {
        return "Product{id=%s, sku=%s, name=%s, quantityOnHand=%d, discontinued=%b}"
                .formatted(getId(), sku, name, quantityOnHand, discontinued);
    }
}
