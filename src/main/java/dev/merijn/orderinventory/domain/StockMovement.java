package dev.merijn.orderinventory.domain;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.Objects;

/**
 * A change in the stock of one product, with the reason for it. Together these movements form the history of
 * the stock; the quantity on a product is a running total of them.
 *
 * Movements are never changed or deleted. To undo a sale or a return, a new movement is added that does the
 * opposite.
 *
 * @author Merijn van Rossum
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

    /** Creates a movement that isn't linked to an order. */
    public StockMovement(final Product product, final int change, final MovementReason reason,
                         final Instant occurredAt) {
        this(product, change, reason, occurredAt, null);
    }

    /** Creates a movement. The change can't be zero, and only a sale can be linked to an order. */
    public StockMovement(final Product product, final int change, final MovementReason reason,
                         final Instant occurredAt, final Order originatingOrder) {
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

    /** Positive for a delivery or restock, negative for a sale. A correction can be either. */
    public int getChange() {
        return change;
    }

    public MovementReason getReason() {
        return reason;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    /** The order that caused this movement. Null unless it was a sale. */
    public Order getOriginatingOrder() {
        return originatingOrder;
    }

    @Override
    public String toString() {
        return "StockMovement{id=%s, product=%s, change=%+d, reason=%s, occurredAt=%s}"
                .formatted(getId(), product == null ? null : product.getSku(), change, reason, occurredAt);
    }
}
