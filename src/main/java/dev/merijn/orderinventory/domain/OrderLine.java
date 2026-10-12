package dev.merijn.orderinventory.domain;

import dev.merijn.orderinventory.validation.NonNegativeAmount;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * One product in a certain quantity on an order. A line only exists as part of its order.
 *
 * The unit price is copied from the product when the line is created, so a later price change doesn't affect
 * orders that were already placed.
 *
 * @author Merijn van Rossum
 */
@Entity
@Table(name = "ORDER_LINES")
public class OrderLine extends AbstractEntity {

    @ManyToOne
    private Order order;
    @ManyToOne
    private Product product;
    @Positive
    private int quantity;
    @NotNull
    @NonNegativeAmount
    private Money unitPrice;

    protected OrderLine() {
        // required by JPA / proxies
    }

    /** Creates a line and copies the product's current price. The quantity must be at least 1. */
    public OrderLine(final Product product, final int quantity) {
        this.product = Objects.requireNonNull(product, "product");
        if (quantity < 1) {
            throw new IllegalArgumentException("order line quantity must be at least 1, was " + quantity);
        }
        this.quantity = quantity;
        this.unitPrice = product.getUnitPrice(); // a copy, so later price changes don't affect this line
    }

    /** The total for this line: quantity times unit price. */
    public Money lineTotal() {
        // TODO(Records mark): replace with unitPrice.times(quantity) once Money arithmetic exists.
        return new Money(
                unitPrice.amount().multiply(BigDecimal.valueOf(quantity)),
                unitPrice.currency());
    }

    /** Sets the order this line belongs to. Only Order.addLine should call this. */
    void assignToOrder(final Order order) {
        this.order = Objects.requireNonNull(order, "order");
    }

    public Order getOrder() {
        return order;
    }

    public Product getProduct() {
        return product;
    }

    public int getQuantity() {
        return quantity;
    }

    public Money getUnitPrice() {
        return unitPrice;
    }

    @Override
    public String toString() {
        return "OrderLine{id=%s, product=%s, quantity=%d}"
                .formatted(getId(), product == null ? null : product.getSku(), quantity);
    }
}
