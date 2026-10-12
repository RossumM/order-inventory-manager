package dev.merijn.orderinventory.domain;

import dev.merijn.orderinventory.repository.ProductRepository;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.Currency;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Bean Validation constraints on Product are enforced when it is saved, so invalid data never reaches
 * the database.
 *
 * @author Merijn van Rossum
 */
@SpringBootTest
class ProductConstraintsIntegrationTest {

    @Autowired
    private ProductRepository products;

    private static Money eur(final String amount) {
        return new Money(new BigDecimal(amount), Currency.getInstance("EUR"));
    }

    @Test
    void blankSkuThrowsConstraintViolationException() {
        assertThatThrownBy(() -> products.saveAndFlush(new Product("  ", "Widget", eur("2.00"))))
                .isInstanceOfSatisfying(ConstraintViolationException.class, e ->
                        assertThat(e.getConstraintViolations())
                                .extracting(violation -> violation.getPropertyPath().toString())
                                .containsExactly("sku"));
    }

    @Test
    void negativeUnitPriceThrowsConstraintViolationException() {
        assertThatThrownBy(() -> products.saveAndFlush(new Product("SKU-NEG", "Widget", eur("-1.00"))))
                .isInstanceOfSatisfying(ConstraintViolationException.class, e ->
                        assertThat(e.getConstraintViolations())
                                .extracting(violation -> violation.getPropertyPath().toString())
                                .containsExactly("unitPrice"));
    }
}
