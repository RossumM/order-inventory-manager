package dev.merijn.orderinventory.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.math.BigDecimal;
import java.util.Currency;

@Embeddable
public record Money(@Column(precision = 19, scale = 2) BigDecimal amount,
                    @Column(length = 3) Currency currency) {
}