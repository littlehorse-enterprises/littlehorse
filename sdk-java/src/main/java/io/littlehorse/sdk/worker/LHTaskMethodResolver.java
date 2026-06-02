package io.littlehorse.sdk.worker;

import io.littlehorse.sdk.worker.internal.util.PlaceholderUtil;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;

/**
 * Strategy interface for resolving which {@link Method} on a task executable should be invoked for a given
 * task definition name.
 *
 * <p>The default implementation ({@link #lHTaskMethodAnnotationResolver()}) scans the executable for a public
 * method annotated with {@link LHTaskMethod} whose resolved value matches the provided task definition name.
 * Custom implementations can be supplied to change this strategy — e.g. lookup by custom annotation, naming
 * convention etc.
 *
 */
@FunctionalInterface
public interface LHTaskMethodResolver {

    /**
     * Resolves the method to invoke for task execution.
     *
     * @param executable the object containing the task implementation
     * @param taskDefName the resolved task definition name
     * @param placeholderValues key-value pairs used to resolve placeholders during method resolution
     * @return the resolved LH task method and its metadata
     * @throws IllegalArgumentException if no matching method is found or if resolution fails
     */
    LHTaskMethodHandle resolve(Object executable, String taskDefName, Map<String, String> placeholderValues);

    /**
     * Returns the default annotation based resolver.
     *
     * <p>This implementation scans all public methods of the executable and
     * selects the one annotated with {@link LHTaskMethod} whose annotation value,
     * after placeholder substitution matches the provided task definition name.
     *
     * @return the default {@link LHTaskMethodResolver}
     * @throws IllegalArgumentException if no matching annotated method is found
     */
    static LHTaskMethodResolver lHTaskMethodAnnotationResolver() {
        return (executable, taskDefName, placeholderValues) -> {
            Method taskMethod = Arrays.stream(executable.getClass().getMethods())
                    .filter(m -> m.isAnnotationPresent(LHTaskMethod.class))
                    .filter(m -> {
                        String annotationValue =
                                m.getAnnotation(LHTaskMethod.class).value();
                        try {
                            return PlaceholderUtil.replacePlaceholders(annotationValue, placeholderValues)
                                    .equals(taskDefName);
                        } catch (IllegalArgumentException ex) {
                            return false;
                        }
                    })
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Provided executable object must have exactly one method annotated"
                                    + " with @LHTaskMethod"));
            return LHTaskMethodHandle.fromLHTaskMethod(taskMethod, placeholderValues);
        };
    }
}
