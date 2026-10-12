package dev.merijn.orderinventory.handmade.validation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Validates any object by looking at its annotated fields, inherited ones included. For every constraint
 * annotation it runs the Checker named in the annotation's CheckerBinding, so the validator itself doesn't know
 * any specific constraint.
 *
 * @author Merijn van Rossum
 */
public class Validator {

    private final Map<Class<? extends Checker>, Checker> checkers = new ConcurrentHashMap<>();

    /**
     * Validates all fields of the object, including inherited ones.
     *
     * @return all violations found, or an empty list if the object is valid
     * @throws IllegalStateException if a constraint is placed on a static field
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
                    "Cannot instantiate checker %s (needs an accessible no-arg constructor)"
                            .formatted(type.getName()), e);
        }
    }
}
