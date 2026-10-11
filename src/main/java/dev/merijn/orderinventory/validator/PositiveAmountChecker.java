package dev.merijn.orderinventory.validator;

import dev.merijn.orderinventory.domain.Money;

import java.lang.annotation.Annotation;
import java.math.BigDecimal;

/**
 * @author Merijn van Rossum
 */
public class PositiveAmountChecker implements Checker {
    @Override
    public boolean isValid(Object value, Annotation annotation) {
        return switch (value) {
            case null -> true;
            case BigDecimal decimal -> BigDecimal.ZERO.compareTo(decimal) <= 0;
            case Money money -> BigDecimal.ZERO.compareTo(money.amount()) <= 0;
            default -> throw new IllegalArgumentException("Unexpected value: " + value);
        };
    }

    @Override
    public String message(Annotation annotation, Object value) {
        return ((PositiveAmount) annotation).message();
    }
}
