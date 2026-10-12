package dev.merijn.orderinventory.handmade.validation;

/**
 * One broken constraint: the field it was found on and the message to show.
 *
 * @author Merijn van Rossum
 */
public record Violation(String field, String message) {
}
