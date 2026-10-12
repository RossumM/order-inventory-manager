package dev.merijn.orderinventory.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.math.BigDecimal;
import java.util.Currency;

/**
 * An amount of money in a certain currency. Negative amounts are allowed; whether an amount must be positive is
 * up to the field that holds it.
 *
 * @author Merijn van Rossum
 */
@Embeddable
public record Money(@Column(precision = 19, scale = 2) BigDecimal amount,
                    @Column(length = 3) Currency currency) {
}
