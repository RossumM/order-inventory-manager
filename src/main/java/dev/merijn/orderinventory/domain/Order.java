package dev.merijn.orderinventory.domain;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * A sale: a customer buying one or more products at a certain moment.
 *
 * The total is calculated from the lines, but also stored so it doesn't have to be recalculated every time. It
 * is refreshed whenever a line is added. Making sure an order has at least one line is up to whoever places it.
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

    /** Creates a new order with status PLACED. */
    public Order(final Instant placedAt) {
        this.placedAt = Objects.requireNonNull(placedAt, "placedAt");
        this.status = OrderStatus.PLACED;
    }

    /** Adds a line, links it to this order and updates the total. */
    public void addLine(final OrderLine line) {
        Objects.requireNonNull(line, "line");
        line.assignToOrder(this);
        orderLines.add(line);
        recalculateTotal();
    }

    /** Read-only view of the lines. Use addLine to add one, so the total stays correct. */
    public List<OrderLine> getOrderLines() {
        return Collections.unmodifiableList(orderLines);
    }

    /** Recalculates the total as the sum of all line totals. */
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

    /** Moves the order from PLACED to CONFIRMED. Any other status throws an IllegalStateException. */
    public void confirm() {
        requireStatus(OrderStatus.PLACED, "confirm");
        this.status = OrderStatus.CONFIRMED;
    }

    /** Moves the order from CONFIRMED to FULFILLED. Any other status throws an IllegalStateException. */
    public void fulfil() {
        requireStatus(OrderStatus.CONFIRMED, "fulfil");
        this.status = OrderStatus.FULFILLED;
    }

    /** Cancels the order. A fulfilled order can't be cancelled (throws an IllegalStateException). */
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

    @Override
    public String toString() {
        return "Order{id=%s, placedAt=%s, status=%s, lines=%d, total=%s}"
                .formatted(getId(), placedAt, status, orderLines.size(), total);
    }
}
