package dev.merijn.orderinventory.handmade.validation;

import java.lang.annotation.Annotation;

/**
 * The rule behind one constraint annotation. It receives the annotation itself, so it can read settings like
 * min and max.
 *
 * @author Merijn van Rossum
 */
public interface Checker {

    /** Returns whether the value of an annotated field satisfies the constraint. */
    boolean isValid(Object value, Annotation annotation);

    /** Builds the message for an invalid value. Only called after isValid returned false. */
    String message(Annotation annotation, Object value);
}
