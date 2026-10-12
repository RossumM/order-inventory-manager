package dev.merijn.orderinventory.handmade.validation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A BigDecimal or Money field must be zero or more; zero counts as positive, like in financial reports. Null is
 * allowed.
 *
 * @author Merijn van Rossum
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@CheckerBinding(validatedBy = PositiveAmountChecker.class)
public @interface PositiveAmount {

    String message() default "must be greater than or equal to 0";
}
