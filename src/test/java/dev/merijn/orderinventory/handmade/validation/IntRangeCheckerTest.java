package dev.merijn.orderinventory.handmade.validation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.lang.annotation.Annotation;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the IntRange rules on their own (both bounds included, null is valid), plus one test that the validator
 * enforces the annotation.
 *
 * @author Merijn van Rossum
 */
class IntRangeCheckerTest {

    private final IntRangeChecker checker = new IntRangeChecker();

    static class Holder {
        @IntRange(min = 10, max = 20)
        private int bounded;
        @IntRange(min = 1)
        private int atLeastOne;
        @IntRange(max = 5)
        private int atMostFive;
    }

    static class OutOfRange {
        @IntRange(min = 1)
        private int quantity = 0;
    }

    private static final Annotation BOUNDED = annotationOnHolder("bounded");
    private static final Annotation AT_LEAST_ONE = annotationOnHolder("atLeastOne");
    private static final Annotation AT_MOST_FIVE = annotationOnHolder("atMostFive");

    private static Annotation annotationOnHolder(final String field) {
        try {
            return Holder.class.getDeclaredField(field).getAnnotation(IntRange.class);
        } catch (NoSuchFieldException e) {
            throw new IllegalStateException(e);
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {10, 15, 20})
    void valuesWithinTheBoundsAreValidIncludingTheBoundsThemselves(final int value) {
        assertThat(checker.isValid(value, BOUNDED)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(ints = {Integer.MIN_VALUE, 0, 9, 21, Integer.MAX_VALUE})
    void valuesOutsideTheBoundsAreInvalid(final int value) {
        assertThat(checker.isValid(value, BOUNDED)).isFalse();
    }

    @Test
    void anOmittedBoundIsOpenEnded() {
        assertThat(checker.isValid(Integer.MAX_VALUE, AT_LEAST_ONE)).isTrue();
        assertThat(checker.isValid(0, AT_LEAST_ONE)).isFalse();

        assertThat(checker.isValid(Integer.MIN_VALUE, AT_MOST_FIVE)).isTrue();
        assertThat(checker.isValid(6, AT_MOST_FIVE)).isFalse();
    }

    @Test
    void nullIsValid() {
        assertThat(checker.isValid(null, BOUNDED)).isTrue();
    }

    @Test
    void messageStatesTheBoundsAndTheOffendingValue() {
        assertThat(checker.message(BOUNDED, 42)).contains("10", "20", "42");
    }

    @Test
    void outOfRangeValueIsReportedByTheValidator() {
        assertThat(new Validator().validate(new OutOfRange()))
                .extracting(Violation::field).containsExactly("quantity");
    }
}
