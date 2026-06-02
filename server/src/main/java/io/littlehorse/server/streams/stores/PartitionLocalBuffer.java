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
 * A single-threaded, partition-local write-through buffer for {@link Storeable} items.
 *
 * <p>Items written here are expected to also be persisted to the underlying store by the caller.
 * The buffer serves as a read cache (avoiding store lookups for recently-written items) and as a
 * dirty-set tracker for the {@code PartitionDrainScheduler}, which periodically drains items
 * ready for downstream delivery.
 *
 * <p><b>Thread safety:</b> This class is NOT thread-safe. It must only be accessed from the
 * Kafka Streams {@code CommandProcessor} that owns the instance of this class.
 *
 * @param <T> the type of storeable item being buffered
 */
public class PartitionLocalBuffer<T extends Storeable<?>> {

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
