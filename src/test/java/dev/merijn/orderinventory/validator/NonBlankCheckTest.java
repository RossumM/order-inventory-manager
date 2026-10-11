package dev.merijn.orderinventory.validator;

import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** The check in isolation, no scanner involved. Already green. */
class NonBlankCheckTest {

    private final NonBlankCheck check = new NonBlankCheck();

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
        assertThat(check.isValid(null, ANNOTATION)).isFalse();
        assertThat(check.isValid("", ANNOTATION)).isFalse();
        assertThat(check.isValid("   ", ANNOTATION)).isFalse();
    }

    @Test
    void textWithContentIsValid() {
        assertThat(check.isValid("a", ANNOTATION)).isTrue();
        assertThat(check.isValid("  a  ", ANNOTATION)).isTrue();
    }

    @Test
    void nonStringThrowsIllegalArgumentException() {
        assertThatThrownBy(() -> check.isValid(42, ANNOTATION))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void messageComesFromTheAnnotation() {
        assertThat(check.message(ANNOTATION, "")).isEqualTo("custom message");
    }
}
