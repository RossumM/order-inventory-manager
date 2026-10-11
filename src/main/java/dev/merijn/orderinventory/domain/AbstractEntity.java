package dev.merijn.orderinventory.domain;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;

import java.util.UUID;

/**
 * Shared identity for every persistent entity: a surrogate {@code id} (DB-assigned, for FKs and
 * joins) and a {@code uuid} (assigned at construction, the basis for {@code equals}/{@code hashCode}
 * and the future public identifier). {@code equals}/{@code hashCode} are {@code final} and
 * {@code instanceof}-based so lazy proxies compare equal.
 *
 * <p>Rationale — why not {@code id} or {@code ==} for equality, the random-UUID index cost, the
 * time-ordered-id alternatives — in {@code docs/design-notes.html} §2.
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
