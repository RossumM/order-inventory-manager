package dev.merijn.orderinventory.domain;

/**
 * The category of a {@link StockMovement} — a fixed, known set.
 *
 * <p>{@link #SALE} movements carry a link back to the originating {@link Order};
 * the others do not.
 *
 * @author Merijn van Rossum
 */
public enum MovementReason {
    SALE,
    DELIVERY,
    CORRECTION,
    CUSTOMER_RETURN
}
