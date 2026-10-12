package dev.merijn.orderinventory.handmade.validation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Put this on a constraint annotation to say which Checker enforces it.
 *
 * @author Merijn van Rossum
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface CheckerBinding {
    Class<? extends Checker> validatedBy();
}
