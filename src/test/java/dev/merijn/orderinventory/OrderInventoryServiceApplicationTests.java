package dev.merijn.orderinventory;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author Merijn van Rossum
 */
@SpringBootTest
class OrderInventoryServiceApplicationTests {

    /**
     * Boots the full Spring context against in-memory H2. Passes with zero entities;
     * once Domain Model · Entities lands, this also exercises Hibernate schema generation.
     */
    @Test
    void contextLoads() {
    }
}
