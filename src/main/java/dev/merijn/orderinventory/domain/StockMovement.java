package dev.merijn.orderinventory.domain;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.Objects;

/**
 * One recorded change to one product's stock, with its reason. The append-only ledger
 * that {@link Product#getQuantityOnHand()} is a cache of (I6).
 *
 * <p>Movements are never edited or deleted; the stock effect of a cancelled or returned
 * sale is undone by recording a compensating movement (I8).
 *
 * <p>Persistence mapping (the {@code @ManyToOne} references, the enum column) is intentionally
 * not here yet.
 */
@Entity
@Table(name = "STOCK_MOVEMENTS")
public class StockMovement extends AbstractEntity {

    @ManyToOne
    private Product product;
    private int change;
    @Enumerated(EnumType.STRING)
    private MovementReason reason;
    private Instant occurredAt;
    @ManyToOne
    private Order originatingOrder;

    protected StockMovement() {
        // required by JPA / proxies
    }

    public StockMovement(Product product, int change, MovementReason reason, Instant occurredAt) {
        this(product, change, reason, occurredAt, null);
    }

    public StockMovement(Product product, int change, MovementReason reason, Instant occurredAt,
                         Order originatingOrder) {
        this.product = Objects.requireNonNull(product, "product");
        this.reason = Objects.requireNonNull(reason, "reason");
        this.occurredAt = Objects.requireNonNull(occurredAt, "occurredAt");
        if (change == 0) {
            throw new IllegalArgumentException("a stock movement must change stock by a non-zero amount");
        }
        if (originatingOrder != null && reason != MovementReason.SALE) {
            throw new IllegalArgumentException(
                    "only a SALE movement may carry an originating order, was " + reason);
        }
        this.change = change;
        this.originatingOrder = originatingOrder;
    }

    public Product getProduct() {
        return product;
    }

    /** Signed: negative for a sale, positive for a delivery / restock, either sign for a correction. */
    public int getChange() {
        return change;
    }

    public MovementReason getReason() {
        return reason;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    /** The order that caused this movement, or {@code null} unless {@code reason == SALE}. */
    public Order getOriginatingOrder() {
        return originatingOrder;
    }

    // equals() / hashCode() inherited from AbstractEntity (uuid-based).

    @Override
    public String toString() {
        return "StockMovement{id=%s, product=%s, change=%+d, reason=%s, occurredAt=%s}"
                .formatted(getId(), product == null ? null : product.getSku(), change, reason, occurredAt);
    }
}
