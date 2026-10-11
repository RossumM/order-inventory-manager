package dev.merijn.orderinventory.validator;

import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;
import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Pins the behaviour of {@link Validator}. Most of these fail until {@code validate} is implemented.
 * Note the negative tests: an empty violation list looks identical to "valid", so invalid input must
 * be asserted to produce specific, named violations.
 */
class ValidatorTest {

    private final Validator validator = new Validator();

    // ---- sample classes: private fields on purpose, the validator must cope with that ----

    static class Person {
        @NonBlank
        private String name;
        private String nickname;                    // no annotation: never validated

        Person(final String name, final String nickname) {
            this.name = name;
            this.nickname = nickname;
        }
    }

    static class Employee extends Person {
        @NonBlank
        private String department;

        Employee(final String name, final String nickname, final String department) {
            super(name, nickname);
            this.department = department;
        }
    }

    static class TwoFields {
        @NonBlank
        private String first;
        @NonBlank
        private String second;

        TwoFields(final String first, final String second) {
            this.first = first;
            this.second = second;
        }
    }

    static class Priced {
        @PositiveAmount
        private BigDecimal price;
        @NonBlank
        private String label;

        Priced(final BigDecimal price, final String label) {
            this.price = price;
            this.label = label;
        }
    }

    static class StaticField {
        @NonBlank
        private static String shared = "";          // a constraint on static state is misuse
    }

    static class MisplacedAnnotation {
        @NonBlank
        private Integer count = 1;                  // @NonBlank on a non-String
    }

    // ---- violations are found ----

    @Test
    void nullValueIsReported() {
        List<Violation> violations = validator.validate(new Person(null, "x"));

        assertThat(violations).extracting(Violation::field).containsExactly("name");
    }

    @Test
    void emptyStringIsReported() {
        List<Violation> violations = validator.validate(new Person("", "x"));

        assertThat(violations).extracting(Violation::field).containsExactly("name");
    }

    @Test
    void whitespaceOnlyStringIsReported() {
        List<Violation> violations = validator.validate(new Person("   ", "x"));

        assertThat(violations).extracting(Violation::field).containsExactly("name");
    }

    @Test
    void violationHasTheFieldNameAndTheAnnotationsMessage() throws Exception {
        List<Violation> violations = validator.validate(new Person("", "x"));

        assertThat(violations).containsExactly(new Violation("name", defaultMessage(NonBlank.class)));
    }

    @Test
    void inheritedAndOwnFieldsAreAllReported() {
        List<Violation> violations = validator.validate(new Employee("", "x", ""));

        assertThat(violations).extracting(Violation::field)
                .containsExactlyInAnyOrder("name", "department");
    }

    @Test
    void everyViolationIsCollectedNotJustTheFirst() {
        List<Violation> violations = validator.validate(new TwoFields("", "  "));

        assertThat(violations).extracting(Violation::field)
                .containsExactlyInAnyOrder("first", "second");
    }

    @Test
    void differentConstraintAnnotationsOnOneObjectAreAllReported() throws Exception {
        List<Violation> violations = validator.validate(new Priced(new BigDecimal("-1.00"), ""));

        assertThat(violations).containsExactlyInAnyOrder(
                new Violation("price", defaultMessage(PositiveAmount.class)),
                new Violation("label", defaultMessage(NonBlank.class)));
    }

    // No violations

    @Test
    void validObjectProducesNoViolations() {
        assertThat(validator.validate(new Person("Ann", null))).isEmpty();
        assertThat(validator.validate(new Employee("Ann", null, "Sales"))).isEmpty();
    }

    @Test
    void constraintOnAStaticFieldThrowsIllegalStateException() {
        assertThatThrownBy(() -> validator.validate(new StaticField()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("@NonBlank is not supported on static field StaticField.shared");
    }

    @Test
    void annotationOnAnUnsupportedTypeThrowsIllegalArgumentException() {
        assertThatThrownBy(() -> validator.validate(new MisplacedAnnotation()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private static String defaultMessage(final Class<? extends Annotation> annotation) throws NoSuchMethodException {
        return (String) annotation.getMethod("message").getDefaultValue();
    }
}
