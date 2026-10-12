package dev.merijn.orderinventory.handmade.validation;

import java.lang.annotation.Annotation;

/**
 * @author Merijn van Rossum
 */
public class IntRangeChecker implements Checker {
    @Override
    public boolean isValid(final Object value, final Annotation annotation) {
        if (value == null) {
            return true;
        }
        if (!(value instanceof Integer)) {
            throw new IllegalArgumentException("@IntRange is not supported on " + value.getClass().getName());
        }
        int intValue = (Integer) value;
        return intValue >= getMin(annotation) && intValue <= getMax(annotation);
    }

    private static int getMax(final Object annotation) {
        return ((IntRange) annotation).max();
    }

    private static int getMin(final Object annotation) {
        return ((IntRange) annotation).min();
    }

    @Override
    public String message(final Annotation annotation, final Object value) {
        return ((IntRange) annotation).message()
                .replace("{min}", String.valueOf(getMin(annotation)))
                .replace("{max}", String.valueOf(getMax(annotation)))
                .replace("{actual}", String.valueOf(value));
    }
}
