package dev.merijn.orderinventory.domain;

/**
 * Where an order is in its lifecycle: placed, confirmed, then fulfilled. An order can be cancelled as long as
 * it isn't fulfilled.
 *
 * @author Merijn van Rossum
 */
public enum OrderStatus {
    PLACED,
    CONFIRMED,
    FULFILLED,
    CANCELLED
}
