package io.littlehorse.sdk.worker.adapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Immutable registry of SDK type adapters keyed by Java class.
 */
public class LHTypeAdapterRegistry {
    private final Map<Class<?>, LHTypeAdapter<?>> byClass;

    private LHTypeAdapterRegistry(Map<Class<?>, LHTypeAdapter<?>> byClass) {
        this.byClass = Collections.unmodifiableMap(new LinkedHashMap<>(byClass));
    }

    /**
     * Creates an empty type adapter registry.
     *
     * @return empty registry
     */
    public static LHTypeAdapterRegistry empty() {
        return new LHTypeAdapterRegistry(Collections.emptyMap());
    }

    /**
     * Creates a registry from a list of adapters.
     *
     * @param adapters adapters to register
     * @return registry containing the provided adapters
     */
    public static LHTypeAdapterRegistry from(List<LHTypeAdapter<?>> adapters) {
        if (adapters == null || adapters.isEmpty()) {
            return empty();
        }

        LinkedHashMap<Class<?>, LHTypeAdapter<?>> byClass = new LinkedHashMap<>();
        for (LHTypeAdapter<?> adapter : adapters) {
            Class<?> typeClass = adapter.getTypeClass();
            if (byClass.containsKey(typeClass)) {
                throw new IllegalArgumentException(
                        "A type adapter for " + typeClass.getName() + " is already registered");
            }
            byClass.put(typeClass, adapter);
        }

        return new LHTypeAdapterRegistry(byClass);
    }

    /**
     * Looks up the adapter registered for a Java class.
     *
     * @param clazz Java class key
     * @return adapter if one is registered
     */
    public Optional<LHTypeAdapter<?>> getForClass(Class<?> clazz) {
        return Optional.ofNullable(byClass.get(clazz));
    }

    /**
     * Returns the registered adapters in insertion order.
     *
     * @return immutable list of adapters
     */
    public List<LHTypeAdapter<?>> asList() {
        return Collections.unmodifiableList(new ArrayList<>(byClass.values()));
    }
}
