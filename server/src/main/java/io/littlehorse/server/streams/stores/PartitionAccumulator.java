package io.littlehorse.server.streams.stores;

import io.littlehorse.common.Storeable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * Generic in-memory buffer for partition-local accumulators. Items are keyed by their
 * {@link Storeable#getStoreKey()} and can be drained based on a predicate or all at once.
 *
 * @param <T> the type of storeable item being accumulated
 */
public class PartitionAccumulator<T extends Storeable<?>> {

    private final Map<String, T> items = new HashMap<>();

    public void put(T item) {
        items.put(item.getStoreKey(), item);
    }

    public T get(String storeKey) {
        return items.get(storeKey);
    }

    public boolean hasEntries() {
        return !items.isEmpty();
    }

    public Collection<T> values() {
        return items.values();
    }

    /**
     * Removes and returns all items matching the given predicate.
     */
    public List<T> drain(Predicate<T> readyToFlush) {
        List<T> drained = new ArrayList<>();
        Iterator<T> iterator = items.values().iterator();
        while (iterator.hasNext()) {
            T item = iterator.next();
            if (readyToFlush.test(item)) {
                drained.add(item);
                iterator.remove();
            }
        }
        return drained;
    }

    /**
     * Removes and returns all items.
     */
    public List<T> drainAll() {
        List<T> drained = new ArrayList<>(items.values());
        items.clear();
        return drained;
    }

    public void clear() {
        items.clear();
    }

    public void remove(String key) {
        items.remove(key);
    }
}
