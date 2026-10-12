package dev.merijn.orderinventory.validation;

import dev.merijn.orderinventory.domain.Money;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.math.BigDecimal;

/**
 * Checks the NonNegativeAmount constraint.
 *
 * @author Merijn van Rossum
 */
public class NonNegativeAmountValidator implements ConstraintValidator<NonNegativeAmount, Money> {

    @Override
    public boolean isValid(final Money value, final ConstraintValidatorContext context) {
        return value == null || BigDecimal.ZERO.compareTo(value.amount()) <= 0;
    }
}
