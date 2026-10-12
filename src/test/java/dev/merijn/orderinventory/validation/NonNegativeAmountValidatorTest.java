package dev.merijn.orderinventory.validation;

import dev.merijn.orderinventory.domain.Money;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the NonNegativeAmount rules through the real Bean Validation validator: zero counts as positive,
 * null is valid.
 *
 * @author Merijn van Rossum
 */
class NonNegativeAmountValidatorTest {

    private static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();

    static class Holder {
        @NonNegativeAmount
        private Money amount;

        Holder(final Money amount) {
            this.amount = amount;
        }
    }

    private static Money eur(final String amount) {
        return new Money(new BigDecimal(amount), Currency.getInstance("EUR"));
    }

    private static Set<ConstraintViolation<Holder>> violationsFor(final Money amount) {
        return VALIDATOR.validate(new Holder(amount));
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "0.0", "0.00", "0.01", "5", "1234567.89"})
    void zeroAndPositiveAmountsAreValid(final String amount) {
        assertThat(violationsFor(eur(amount))).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"-0.01", "-1", "-100.50"})
    void negativeAmountsAreReported(final String amount) {
        assertThat(violationsFor(eur(amount)))
                .extracting(violation -> violation.getPropertyPath().toString())
                .containsExactly("amount");
    }

    @Test
    void nullIsValid() {
        assertThat(violationsFor(null)).isEmpty();
    }

    @Test
    void negativeAmountIsReportedWithTheDefaultMessage() {
        assertThat(violationsFor(eur("-1")))
                .extracting(ConstraintViolation::getMessage)
                .containsExactly("must be greater than or equal to 0");
    }
}
