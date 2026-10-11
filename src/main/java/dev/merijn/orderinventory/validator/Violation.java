package dev.merijn.orderinventory.validator;

/** One broken constraint: the field it was found on and the message to show. */
public record Violation(String field, String message) {
}
