package io.littlehorse.test.internal;

import io.littlehorse.test.exception.LHTestInitializationException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class FieldDependencyInjector {

    private final List<Field> candidates;
    private final Supplier<Object> dependency;

    private final Predicate<Field> condition;

    private final Object target;

    public FieldDependencyInjector(
        Supplier<Object> dependency,
        Object target,
        Predicate<Field> condition
    ) {
        candidates = Arrays.asList(target.getClass().getDeclaredFields());
        this.dependency = dependency;
        this.condition = condition;
        this.target = target;
    }

    public void inject() {
        candidates.stream().filter(condition).forEach(this::injectDependency);
    }

    private void injectDependency(Field targetField) {
        targetField.setAccessible(true);
        try {
            targetField.set(target, dependency.get());
        } catch (IllegalAccessException e) {
            throw new LHTestInitializationException(
                String.format(
                    "Not possible to write %s field",
                    targetField.getName()
                ),
                e
            );
        }
    }
}
