package dev.merijn.orderinventory.validator;

import java.lang.annotation.Annotation;

/**
 * The rule behind one constraint annotation. Receives the annotation instance so a check can read
 * its members (e.g. {@code min}/{@code max}).
 */
public interface Check {

    boolean isValid(Object value, Annotation annotation);

    /** Called only after {@link #isValid} returned false; {@code value} may be null if null is invalid. */
    String message(Annotation annotation, Object value);
}
