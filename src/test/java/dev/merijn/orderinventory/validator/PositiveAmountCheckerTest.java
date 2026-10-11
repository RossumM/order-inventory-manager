package dev.merijn.orderinventory.validator;

import dev.merijn.orderinventory.domain.Money;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.lang.annotation.Annotation;
import java.math.BigDecimal;
import java.util.Currency;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The {@code @PositiveAmount} rules in isolation (zero counts as positive, as in financial reports; null is
 * valid), plus one test that the validator enforces the annotation.
 *
 * @author Merijn van Rossum
 */
class PositiveAmountCheckerTest {

    private final PositiveAmountChecker checker = new PositiveAmountChecker();

    static class Holder {
        @PositiveAmount(message = "custom message")
        private BigDecimal amount;
    }

    static class Negative {
        @PositiveAmount(message = "custom message")
        private BigDecimal amount = new BigDecimal("-1");
    }

    private static final Annotation ANNOTATION = annotationOnHolder();   // immutable, so safe to share

    private static Annotation annotationOnHolder() {
        try {
            return Holder.class.getDeclaredField("amount").getAnnotation(PositiveAmount.class);
        } catch (NoSuchFieldException e) {
            throw new IllegalStateException(e);
        }
    }

    private static Money eur(final String amount) {
        return new Money(new BigDecimal(amount), Currency.getInstance("EUR"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "0.0", "0.00", "0.01", "5", "1234567.89"})
    void zeroAndPositiveAmountsAreValid(final String amount) {
        assertThat(checker.isValid(new BigDecimal(amount), ANNOTATION)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"-0.01", "-1", "-100.50"})
    void negativeAmountsAreInvalid(final String amount) {
        assertThat(checker.isValid(new BigDecimal(amount), ANNOTATION)).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "0.00", "0.01", "5"})
    void moneyWithZeroOrPositiveAmountIsValid(final String amount) {
        assertThat(checker.isValid(eur(amount), ANNOTATION)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"-0.01", "-100.50"})
    void moneyWithNegativeAmountIsInvalid(final String amount) {
        assertThat(checker.isValid(eur(amount), ANNOTATION)).isFalse();
    }

    @Test
    void nullIsValid() {
        assertThat(checker.isValid(null, ANNOTATION)).isTrue();
    }

    @Test
    void messageComesFromTheAnnotation() {
        assertThat(checker.message(ANNOTATION, new BigDecimal("-1"))).isEqualTo("custom message");
    }

    @Test
    void negativeAmountIsReportedByTheValidator() {
        assertThat(new Validator().validate(new Negative()))
                .containsExactly(new Violation("amount", "custom message"));
    }
}
