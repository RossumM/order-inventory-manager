package dev.merijn.orderinventory.domain;

/**
 * Where an {@link Order} sits in its lifecycle.
 *
 * <p>Deliberately small and explicit: later marks (audit aspect, Spring events,
 * transactional stock changes) hang side effects off these transitions.
 * Legal moves: {@code PLACED -> CONFIRMED -> FULFILLED}, and {@code PLACED}/{@code CONFIRMED -> CANCELLED}.
 */
public enum OrderStatus {
    PLACED,
    CONFIRMED,
    FULFILLED,
    CANCELLED
}
