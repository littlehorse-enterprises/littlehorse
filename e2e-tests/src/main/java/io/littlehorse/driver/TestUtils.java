package io.littlehorse.driver;

import static org.reflections.scanners.Scanners.SubTypes;

import io.littlehorse.tests.Test;
import java.lang.reflect.Modifier;
import java.util.Set;
import java.util.stream.Collectors;
import org.reflections.Reflections;

public final class TestUtils {

    private TestUtils() {}

    public static Set<Class<?>> getAllTestClasses(Set<String> filter) {
        Reflections reflections = new Reflections("io.littlehorse.tests");

        return reflections.get(SubTypes.of(Test.class).asClass()).stream()
                .filter(aClass -> !Modifier.isAbstract(aClass.getModifiers()))
                .filter(
                        aClass ->
                                filter == null
                                        || filter.isEmpty()
                                        || filter.contains(aClass.getSimpleName()))
                .collect(Collectors.toUnmodifiableSet());
    }
}
