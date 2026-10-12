package dev.merijn.orderinventory.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A Money field must be zero or more; zero counts as positive, like in financial reports. Null is allowed.
 *
 * @author Merijn van Rossum
 */
@Documented
@Constraint(validatedBy = NonNegativeAmountValidator.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface NonNegativeAmount {

    String message() default "must be greater than or equal to 0";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
