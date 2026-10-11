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
@CheckerBinding(validatedBy = IntRangeChecker.class)
public @interface IntRange {
    int min() default Integer.MIN_VALUE;

    int max() default Integer.MAX_VALUE;

    String message() default "Range must be between {min} and {max}, but is: {actual}";
}
