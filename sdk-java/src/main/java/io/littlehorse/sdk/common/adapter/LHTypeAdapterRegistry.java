package io.littlehorse.sdk.common.adapter;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class LHTypeAdapterRegistry {
    private final Map<Class<?>, LHTypeAdapter<?>> byClass;

    private LHTypeAdapterRegistry(Map<Class<?>, LHTypeAdapter<?>> byClass) {
        this.byClass = Collections.unmodifiableMap(new LinkedHashMap<>(byClass));
    }

    public static LHTypeAdapterRegistry empty() {
        return new LHTypeAdapterRegistry(Collections.emptyMap());
    }

    public static LHTypeAdapterRegistry from(Map<Class<?>, LHTypeAdapter<?>> byClass) {
        if (byClass == null || byClass.isEmpty()) {
            return empty();
        }

        LinkedHashMap<Class<?>, LHTypeAdapter<?>> map = new LinkedHashMap<>();
        for (Map.Entry<Class<?>, LHTypeAdapter<?>> entry : byClass.entrySet()) {
            Class<?> typeClass = entry.getKey();
            LHTypeAdapter<?> adapter = Objects.requireNonNull(entry.getValue(), "Type adapter cannot be null");
            if (typeClass == null && adapter != null) {
                typeClass = adapter.getTypeClass();
            }

            if (typeClass == null) {
                throw new IllegalArgumentException("Cannot register a type adapter with null class key");
            }
            if (adapter == null) {
                throw new IllegalArgumentException("Cannot register null type adapter for " + typeClass.getName());
            }
            if (map.containsKey(typeClass)) {
                throw new IllegalArgumentException(
                        "A type adapter for " + typeClass.getName() + " is already registered");
            }
            map.put(typeClass, adapter);
        }

        return new LHTypeAdapterRegistry(map);
    }

    public Optional<LHTypeAdapter<?>> getForClass(Class<?> clazz) {
        return Optional.ofNullable(byClass.get(clazz));
    }

    public Map<Class<?>, LHTypeAdapter<?>> asMap() {
        return byClass;
    }
}
