package dev.merijn.orderinventory.validator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Merijn van Rossum
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@CheckerBinding(validatedBy = NonBlankChecker.class)
public @interface NonBlank {

    String message() default "must not be null, empty or blank";
}
