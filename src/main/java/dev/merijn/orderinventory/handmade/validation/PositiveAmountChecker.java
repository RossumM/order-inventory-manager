package dev.merijn.orderinventory.handmade.validation;

import dev.merijn.orderinventory.domain.Money;

import java.lang.annotation.Annotation;
import java.math.BigDecimal;

/**
 * @author Merijn van Rossum
 */
public class PositiveAmountChecker implements Checker {
    @Override
    public boolean isValid(final Object value, final Annotation annotation) {
        return switch (value) {
            case null -> true;
            case BigDecimal decimal -> BigDecimal.ZERO.compareTo(decimal) <= 0;
            case Money money -> BigDecimal.ZERO.compareTo(money.amount()) <= 0;
            default -> throw new IllegalArgumentException(
                    "@PositiveAmount is not supported on " + value.getClass().getName());
        };
    }

    @Override
    public String message(final Annotation annotation, final Object value) {
        return ((PositiveAmount) annotation).message();
    }
}
