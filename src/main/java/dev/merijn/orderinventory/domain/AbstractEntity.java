package dev.merijn.orderinventory.domain;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;

import java.util.UUID;

/**
 * Base class for all entities. Every entity has two identifiers: an id that the database assigns, and a uuid
 * that is generated when the object is created. The uuid is also the one to expose to the outside later.
 *
 * equals and hashCode are based on the uuid. The id can't be used for that, because it is null until the entity
 * is saved, so the hash would change after saving.
 *
 * @author Merijn van Rossum
 */
@MappedSuperclass
public abstract class AbstractEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false)
    private UUID uuid = UUID.randomUUID();

    protected AbstractEntity() {
        // Hibernate instantiates here, then overwrites both fields from the row.
    }

    public Long getId() {
        return id;
    }

    public UUID getUuid() {
        return uuid;
    }

    @Override
    public final boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AbstractEntity other)) {
            return false;
        }

        return uuid.equals(other.uuid);
    }

    @Override
    public final int hashCode() {
        return uuid.hashCode();
    }
}
