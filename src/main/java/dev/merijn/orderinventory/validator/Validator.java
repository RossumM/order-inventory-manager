package dev.merijn.orderinventory.validator;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Generic engine: scans any object's fields for constraint annotations (those carrying
 * {@link CheckerBinding}) and runs the {@link Checker} each one names. Knows no concrete constraint.
 * Walkthrough: docs/walkthrough-validation.html.
 *
 * @author Merijn van Rossum
 */
public class Validator {

    private final Map<Class<? extends Checker>, Checker> checkers = new ConcurrentHashMap<>();

    /**
     * @return every violation found on {@code target}; an empty list if it is valid
     */
    public List<Violation> validate(final Object target) {
        List<Violation> violations = new ArrayList<>();

        Class<?> targetClass = target.getClass();
        while (targetClass != null && targetClass != Object.class) {
            for (Field field : targetClass.getDeclaredFields()) {
                if (field.isSynthetic()) {
                    continue;
                }
                for (Annotation annotation : field.getAnnotations()) {
                    CheckerBinding binding = annotation.annotationType().getAnnotation(CheckerBinding.class);
                    if (binding == null) {
                        continue;
                    }
                    if (Modifier.isStatic(field.getModifiers())) {
                        throw new IllegalStateException("@%s is not supported on static field %s.%s"
                                .formatted(annotation.annotationType().getSimpleName(),
                                        targetClass.getSimpleName(), field.getName()));
                    }
                    Checker checker = checkers.computeIfAbsent(binding.validatedBy(), Validator::instantiate);
                    Object value = read(target, field);
                    if (!checker.isValid(value, annotation)) {
                        violations.add(new Violation(field.getName(), checker.message(annotation, value)));
                    }
                }
            }
            targetClass = targetClass.getSuperclass();
        }
        return violations;
    }

    private static Object read(final Object target, final Field field) {
        try {
            field.setAccessible(true);
            return field.get(target);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Cannot access field " + field.getName(), e);
        }
    }

    private static Checker instantiate(final Class<? extends Checker> type) {
        try {
            return type.getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(
                    "Cannot instantiate checker %s (needs an accessible no-arg constructor)".formatted(type.getName()), e);
        }
    }
}
