package dev.merijn.orderinventory.domain;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * A single sale: a customer taking one or more products at a point in time.
 *
 * <p>{@code total} is derived from the lines (I7) but kept as a running value for fast reads (P1);
 * {@link #recalculateTotal()} is called whenever the line set changes so the two never drift.
 * An order with zero lines is not valid (I2) — enforced at placement time by the caller, not here.
 *
 * @author Merijn van Rossum
 */
@Entity
@Table(name = "ORDERS")
public class Order extends AbstractEntity {

    private Instant placedAt;
    @Enumerated(EnumType.STRING)
    private OrderStatus status;
    @OneToMany(mappedBy = "order")
    private List<OrderLine> orderLines = new ArrayList<>();
    private Money total;

    protected Order() {
        // required by JPA / proxies
    }

    public Order(final Instant placedAt) {
        this.placedAt = Objects.requireNonNull(placedAt, "placedAt");
        this.status = OrderStatus.PLACED;
    }

    public void addLine(final OrderLine line) {
        Objects.requireNonNull(line, "line");
        line.assignToOrder(this);
        orderLines.add(line);
        recalculateTotal();
    }

    /** Unmodifiable view — add lines through {@link #addLine} so the total stays in step. */
    public List<OrderLine> getOrderLines() {
        return Collections.unmodifiableList(orderLines);
    }

    /** Recompute {@code total} as the sum of the line totals (I7). */
    public void recalculateTotal() {
        if (orderLines.isEmpty()) {
            this.total = null;
            return;
        }
        // TODO(Records mark): fold with Money.plus once Money arithmetic exists.
        BigDecimal sum = BigDecimal.ZERO;
        for (OrderLine line : orderLines) {
            sum = sum.add(line.lineTotal().amount());
        }
        this.total = new Money(sum, orderLines.getFirst().lineTotal().currency());
    }

    // --- status lifecycle: PLACED -> CONFIRMED -> FULFILLED, or -> CANCELLED ---

    public void confirm() {
        requireStatus(OrderStatus.PLACED, "confirm");
        this.status = OrderStatus.CONFIRMED;
    }

    public void fulfil() {
        requireStatus(OrderStatus.CONFIRMED, "fulfil");
        this.status = OrderStatus.FULFILLED;
    }

    public void cancel() {
        if (status == OrderStatus.FULFILLED) {
            throw new IllegalStateException("a fulfilled order cannot be cancelled");
        }
        this.status = OrderStatus.CANCELLED;
    }

    private void requireStatus(final OrderStatus expected, final String action) {
        if (status != expected) {
            throw new IllegalStateException(
                    "cannot %s an order in status %s (expected %s)".formatted(action, status, expected));
        }
    }

    public Instant getPlacedAt() {
        return placedAt;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public Money getTotal() {
        return total;
    }

    // equals() / hashCode() inherited from AbstractEntity (uuid-based).

    @Override
    public String toString() {
        return "Order{id=%s, placedAt=%s, status=%s, lines=%d, total=%s}"
                .formatted(getId(), placedAt, status, orderLines.size(), total);
    }
}
