package dev.merijn.orderinventory.domain;

/**
 * Why a stock movement happened. Only a sale is linked to the order that caused it.
 *
 * @author Merijn van Rossum
 */
public enum MovementReason {
    SALE,
    DELIVERY,
    CORRECTION,
    CUSTOMER_RETURN
}
