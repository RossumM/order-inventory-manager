# Order & Inventory Service

A small backend service for orders and stock, built as a learning project. The goal is not a product but to
understand the mechanisms behind enterprise Java: reflection, proxies, transactions, concurrency, data access.
The domain (products, orders, stock movements) is kept simple so the attention goes to the technology.

**Stack:** Java 25, Spring Boot 4, Maven, H2 (in-memory) for now.

```
mvn test
```

## Implemented

- **Domain model:** `Product`, `Order`, `OrderLine`, `StockMovement` as JPA entities with a shared base class
  (`AbstractEntity`). Every entity has a database id and a UUID that exists from creation; equals and hashCode
  use the UUID, so they stay the same before and after saving.
- **Money** as an embeddable record. Negative amounts are allowed; whether an amount must be positive is a rule of
  the field that holds it.
- **Spring Data repositories** and a `StockService` that records stock movements (constructor injection, no
  transactions yet on purpose).
- **Validation, twice:** a hand-built validator and the real Bean Validation, see below.

## Validation

### The hand-built validator (`handmade.validation`)

A simplified version of Bean Validation, written to see how it works underneath. It is kept as a demo and is not
used by the service.

- A constraint annotation (`@NonBlank`, `@PositiveAmount`, `@IntRange`) only describes the rule. It is marked with
  `@CheckerBinding(validatedBy = ...)`, which names the `Checker` that enforces it.
- The `Validator` uses reflection to walk the fields of an object (superclasses included), reads each field's
  annotations, looks up the checker, and collects a `Violation` for every field that fails. It does not know any
  specific constraint, so adding one needs no change to the validator.
- Checkers are created through their no-argument constructor and cached.

### Bean Validation (`validation`)

The service uses Jakarta Bean Validation (Hibernate Validator). `@NonNegativeAmount` is a custom constraint
written the same way as a built-in one: an annotation plus a `ConstraintValidator`. It is used on the unit prices
of `Product` and `OrderLine`, next to the standard `@NotBlank`, `@NotNull` and `@Positive`. Hibernate validates
entities automatically before saving, so an invalid entity fails with a `ConstraintViolationException` before it
reaches the database.

### Differences

The hand-built version covers the core loop only. Bean Validation adds message templates that can be translated,
groups, composed constraints, validation of method parameters, following `@Valid` into nested objects, and
validators that receive dependencies from Spring. The hand-built validator does not look inside nested objects.

## Roadmap

Each topic gets built and tested before moving on.

- [x] Domain model and constructor injection
- [x] Reflection: annotations, field scanner, swap to Bean Validation
- [ ] Records: `Money` (compact constructor, factory method, pattern matching)
- [ ] Dynamic proxies: hand-rolled with `java.lang.reflect.Proxy`, then Spring AOP, then JDK proxy vs CGLIB
- [ ] Applied AOP: logging, audit and retry aspects
- [ ] Transactions: order placement, rollback, `REQUIRES_NEW`
- [ ] Concurrency: forced race conditions on stock and a comparison of lock strategies
- [ ] Events: Spring application events
- [ ] Data access: relationships, N+1 and its fixes, indexing, pagination, connection pooling, caching
- [ ] Design patterns: strategy, builder, factory, decorator, observer, singleton, adapter
- [ ] Generics: bounded types, wildcards, a generic repository
- [ ] Later: PostgreSQL with Docker, cloud deployment, horizontal scaling, resilience and observability

There is no frontend. The service is exercised through automated tests, and later through HTTP calls.
