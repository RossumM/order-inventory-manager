package dev.merijn.orderinventory.validator;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Generic engine: scans any object's fields for constraint annotations and runs the matching
 * {@link Check}s. Walkthrough: docs/walkthrough-validation.html.
 */
public class Validator {

    private final Map<Class<? extends Annotation>, Check> checks = new HashMap<>();

    public Validator() {
        checks.put(NonBlank.class, new NonBlankCheck());
        checks.put(PositiveAmount.class, new PositiveAmountCheck());
    }

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
                    if (checks.containsKey(annotation.annotationType())) {
                        if (Modifier.isStatic(field.getModifiers())) {
                            throw new IllegalStateException("@%s is not supported on static field %s.%s"
                                    .formatted(annotation.annotationType().getSimpleName(),
                                            targetClass.getSimpleName(), field.getName()));
                        }
                        try {
                            Check check = checks.get(annotation.annotationType());
                            field.setAccessible(true);
                            if (!check.isValid(field.get(target), annotation)) {
                                violations.add(new Violation(field.getName(), check.message(annotation, field.get(target))));
                            }
                        } catch (IllegalAccessException e) {
                            throw new IllegalStateException("Cannot access field " + field.getName(), e);
                        }
                    }
                }
            }
            targetClass = targetClass.getSuperclass();
        }
        return violations;
    }
}
