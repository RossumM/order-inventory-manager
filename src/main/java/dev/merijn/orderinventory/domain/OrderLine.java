package dev.merijn.orderinventory.domain;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * One product-and-quantity within an {@link Order}.
 *
 * <p>Owned by its order (composition): a line has no meaning outside it. The {@code unitPrice}
 * is a snapshot copied from the {@link Product} when the order is placed and never moves
 * afterwards, even if the product's price later changes (I4).
 *
 * <p>Persistence mapping (the {@code @ManyToOne} to {@code Product}, the back-reference to
 * {@code Order}) is intentionally not here yet — that is the Data Access · JPA Relationships mark.
 */
public class OrderLine {

    private Long id;
    private Order order;
    private Product product;
    private int quantity;
    private Money unitPrice;

    protected OrderLine() {
        // required by JPA / proxies
    }

    public OrderLine(Product product, int quantity) {
        this.product = Objects.requireNonNull(product, "product");
        if (quantity < 1) {
            throw new IllegalArgumentException("order line quantity must be at least 1 (I3), was " + quantity);
        }
        this.quantity = quantity;
        this.unitPrice = product.getUnitPrice(); // snapshot at placement (I4)
    }

    /**
     * {@code quantity} &times; snapshot unit price. Derived, not stored (building block for I7).
     */
    public Money lineTotal() {
        // TODO(Records mark): replace with unitPrice.times(quantity) once Money arithmetic exists.
        return new Money(
                unitPrice.amount().multiply(BigDecimal.valueOf(quantity)),
                unitPrice.currency());
    }

    /** Set the owning order. Package-private: only {@link Order#addLine} should call this. */
    void assignToOrder(Order order) {
        this.order = Objects.requireNonNull(order, "order");
    }

    public Long getId() {
        return id;
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

    // equals() / hashCode() deliberately left undefined — see TODO.md.

    @Override
    public String toString() {
        return "OrderLine{id=%s, product=%s, quantity=%d}"
                .formatted(id, product == null ? null : product.getSku(), quantity);
    }
}
