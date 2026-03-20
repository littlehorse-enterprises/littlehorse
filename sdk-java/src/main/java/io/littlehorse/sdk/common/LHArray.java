package io.littlehorse.sdk.common;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * Typed Java wrapper for native LittleHorse arrays.
 *
 * <p>Use this as an explicit opt-in for native Array semantics. Legacy {@code List}/{@code T[]} task
 * parameters and returns continue to map to {@code JSON_ARR} for compatibility.
 */
public final class LHArray<T> implements Iterable<T> {

    private final List<T> items;

    private LHArray(Collection<T> items) {
        this.items = Collections.unmodifiableList(new ArrayList<>(Objects.requireNonNull(items)));
    }

    public static <T> LHArray<T> of(Collection<T> items) {
        return new LHArray<>(items);
    }

    public static <T> LHArray<T> empty() {
        return new LHArray<>(List.of());
    }

    public List<T> asList() {
        return items;
    }

    public int size() {
        return items.size();
    }

    public T get(int index) {
        return items.get(index);
    }

    @Override
    public Iterator<T> iterator() {
        return items.iterator();
    }

    @Override
    public String toString() {
        return items.toString();
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof LHArray<?>)) {
            return false;
        }
        LHArray<?> rhs = (LHArray<?>) other;
        return items.equals(rhs.items);
    }

    @Override
    public int hashCode() {
        return items.hashCode();
    }
}
