package dev.merijn.orderinventory.validator;

import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * The {@code @NonBlank} rules in isolation, plus one test that the validator enforces the annotation.
 *
 * @author Merijn van Rossum
 */
class NonBlankCheckerTest {

    private final NonBlankChecker checker = new NonBlankChecker();

    static class Holder {
        @NonBlank(message = "custom message")
        private String value;
    }

    private static final Annotation ANNOTATION = annotationOnHolder();   // immutable, so safe to share

    private static Annotation annotationOnHolder() {
        try {
            return Holder.class.getDeclaredField("value").getAnnotation(NonBlank.class);
        } catch (NoSuchFieldException e) {
            throw new IllegalStateException(e);
        }
    }

    @Test
    void nullEmptyAndBlankAreInvalid() {
        assertThat(checker.isValid(null, ANNOTATION)).isFalse();
        assertThat(checker.isValid("", ANNOTATION)).isFalse();
        assertThat(checker.isValid("   ", ANNOTATION)).isFalse();
    }

    @Test
    void textWithContentIsValid() {
        assertThat(checker.isValid("a", ANNOTATION)).isTrue();
        assertThat(checker.isValid("  a  ", ANNOTATION)).isTrue();
    }

    @Test
    void nonStringThrowsIllegalArgumentException() {
        assertThatThrownBy(() -> checker.isValid(42, ANNOTATION))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void messageComesFromTheAnnotation() {
        assertThat(checker.message(ANNOTATION, "")).isEqualTo("custom message");
    }

    @Test
    void blankValueIsReportedByTheValidator() {
        assertThat(new Validator().validate(new Holder()))
                .containsExactly(new Violation("value", "custom message"));
    }
}
