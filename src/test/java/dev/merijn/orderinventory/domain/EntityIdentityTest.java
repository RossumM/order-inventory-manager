package dev.merijn.orderinventory.domain;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Entities are equal when they share a uuid. The uuid exists from the moment an entity is created, so equality
 * stays the same before and after saving. No Spring context needed.
 *
 * @author Merijn van Rossum
 */
class EntityIdentityTest {

    private static Product product(final String sku) {
        return new Product(sku, "name", new Money(new BigDecimal("1.00"), Currency.getInstance("EUR")));
    }

    @Test
    void eachNewEntityGetsItsOwnUuidAtConstruction() {
        Product a = product("SKU-1");
        Product b = product("SKU-1");

        assertThat(a.getUuid()).isNotNull();
        assertThat(a.getUuid()).isNotEqualTo(b.getUuid());
    }

    @Test
    void equalsIsReflexiveButNotBusinessDataBased() {
        Product p = product("SKU-1");
        assertThat(p).isEqualTo(p);

        // same SKU, name, price — but a different object identity
        assertThat(p).isNotEqualTo(product("SKU-1"));
    }

    @Test
    void twoInstancesOfTheSameRowCompareEqual() {
        Product loadedOnce = product("SKU-1");
        Product loadedAgain = product("SKU-1");
        // simulate the same DB row reaching code a second time (detached + reloaded): same uuid column
        ReflectionTestUtils.setField(loadedAgain, "uuid", loadedOnce.getUuid());

        assertThat(loadedAgain).isEqualTo(loadedOnce);
        assertThat(loadedAgain.hashCode()).isEqualTo(loadedOnce.hashCode());
    }

    @Test
    void staysFindableInAHashSetAfterMutation() {
        Product p = product("SKU-1");
        Set<Product> set = new HashSet<>();
        set.add(p);

        p.adjustStock(50);
        p.rename("renamed");
        p.discontinue();

        assertThat(set).contains(p);
        assertThat(set.remove(p)).isTrue();
    }

    @Test
    void notEqualToNullOrForeignType() {
        Product p = product("SKU-1");

        assertThat(p).isNotEqualTo(null);
        assertThat(p.equals("not an entity")).isFalse();
    }
}
