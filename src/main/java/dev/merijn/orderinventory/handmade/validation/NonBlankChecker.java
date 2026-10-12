package dev.merijn.orderinventory.handmade.validation;

import java.lang.annotation.Annotation;

/**
 * Rejects null, empty and whitespace-only strings.
 *
 * @author Merijn van Rossum
 */
public class NonBlankChecker implements Checker {

    @Override
    public boolean isValid(final Object value, final Annotation annotation) {
        if (value == null) {
            return false;                  // unlike @Size, null is a violation here
        }
        if (value instanceof String s) {
            return !s.isBlank();
        }
        throw new IllegalArgumentException("@NonBlank is not supported on " + value.getClass().getName());
    }

    @Override
    public String message(final Annotation annotation, final Object value) {
        return ((NonBlank) annotation).message();
    }
}
