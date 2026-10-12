package dev.merijn.orderinventory.handmade.validation;

import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests the validator itself, using a constraint defined in this class, because the validator doesn't know any
 * real constraint. Each real constraint is tested in its own CheckerTest, including one test that the validator
 * enforces it.
 *
 * @author Merijn van Rossum
 */
class ValidatorTest {

    private final Validator validator = new Validator();

    // ---- a constraint local to this test: a String field must equal "ok" ----

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @CheckerBinding(validatedBy = MustBeOkChecker.class)
    @interface MustBeOk {
        String message() default "must be ok";
    }

    static class MustBeOkChecker implements Checker {
        @Override
        public boolean isValid(final Object value, final Annotation annotation) {
            if (value instanceof String s) {
                return s.equals("ok");
            }
            throw new IllegalArgumentException("@MustBeOk is not supported on " + value);
        }

        @Override
        public String message(final Annotation annotation, final Object value) {
            return ((MustBeOk) annotation).message();
        }
    }

    // ---- sample classes: private fields on purpose, the validator must cope with that ----

    static class Person {
        @MustBeOk
        private String name;
        private String nickname;                    // no annotation: never validated

        Person(final String name, final String nickname) {
            this.name = name;
            this.nickname = nickname;
        }
    }

    static class Employee extends Person {
        @MustBeOk
        private String department;

        Employee(final String name, final String nickname, final String department) {
            super(name, nickname);
            this.department = department;
        }
    }

    static class TwoFields {
        @MustBeOk
        private String first;
        @MustBeOk
        private String second;

        TwoFields(final String first, final String second) {
            this.first = first;
            this.second = second;
        }
    }

    static class StaticField {
        @MustBeOk
        private static String shared = "ok";        // a constraint on static state is misuse
    }

    static class MisplacedAnnotation {
        @MustBeOk
        private Integer count = 1;                  // @MustBeOk on a non-String
    }

    // ---- violations are found ----

    @Test
    void violationHasTheFieldNameAndTheAnnotationsMessage() throws Exception {
        List<Violation> violations = validator.validate(new Person("no", "x"));

        assertThat(violations).containsExactly(new Violation("name", defaultMessage(MustBeOk.class)));
    }

    @Test
    void inheritedAndOwnFieldsAreAllReported() {
        List<Violation> violations = validator.validate(new Employee("no", "x", "no"));

        assertThat(violations).extracting(Violation::field)
                .containsExactlyInAnyOrder("name", "department");
    }

    @Test
    void everyViolationIsCollectedNotJustTheFirst() {
        List<Violation> violations = validator.validate(new TwoFields("no", "no"));

        assertThat(violations).extracting(Violation::field)
                .containsExactlyInAnyOrder("first", "second");
    }

    // ---- no violations ----

    @Test
    void validObjectProducesNoViolations() {
        assertThat(validator.validate(new Person("ok", null))).isEmpty();
        assertThat(validator.validate(new Employee("ok", null, "ok"))).isEmpty();
    }

    // ---- misuse ----

    @Test
    void constraintOnAStaticFieldThrowsIllegalStateException() {
        assertThatThrownBy(() -> validator.validate(new StaticField()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("@MustBeOk is not supported on static field StaticField.shared");
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
