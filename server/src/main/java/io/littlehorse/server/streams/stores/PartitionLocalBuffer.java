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
 * <p>This is not merely a note about data races: readers consult the buffer <i>and then fall back to
 * the store</i> on a miss, while the drain removes an entry from the buffer and deletes it from the
 * store as two separate steps. Those two steps are only atomic with respect to command processing
 * because both run on the Streams thread. Draining this buffer from the per-partition background
 * worker would let an increment land between them, read the stale store value, and double count.
 * That is why the metrics drain is deliberately NOT a {@code PartitionBackgroundJob}.
 *
 * <p>The ownership check below is compiled out when assertions are disabled, so it costs nothing on
 * the hot path in production while failing loudly under test if someone moves this off-thread.
 *
 * @param <T> the type of storeable item being buffered
 */
public class PartitionLocalBuffer<T extends Storeable<?>> {

    private static final boolean ASSERTIONS_ENABLED = assertionsEnabled();

    private final Map<String, T> items = new HashMap<>();

    /**
     * The first thread to touch this buffer. Kafka Streams hands a task to exactly one StreamThread
     * at a time, and {@code ServerTopologyV2} builds a fresh CommandProcessor (hence a fresh buffer)
     * per assignment, so this is stable for the buffer's lifetime.
     */
    private Thread owner;

    public void put(T item) {
        checkOwnerThread();
        items.put(item.getStoreKey(), item);
    }

    public T get(String storeKey) {
        checkOwnerThread();
        return items.get(storeKey);
    }

    public boolean hasEntries() {
        checkOwnerThread();
        return !items.isEmpty();
    }

    public Collection<T> values() {
        checkOwnerThread();
        return items.values();
    }

    /**
     * Removes and returns all items matching the given predicate.
     */
    public List<T> drain(Predicate<T> readyToFlush) {
        checkOwnerThread();
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
        checkOwnerThread();
        List<T> drained = new ArrayList<>(items.values());
        items.clear();
        return drained;
    }

    public void clear() {
        checkOwnerThread();
        items.clear();
    }

    public void remove(String key) {
        checkOwnerThread();
        items.remove(key);
    }

    private void checkOwnerThread() {
        if (!ASSERTIONS_ENABLED) {
            return;
        }
        Thread current = Thread.currentThread();
        if (owner == null) {
            owner = current;
        } else if (owner != current) {
            throw new IllegalStateException("PartitionLocalBuffer is owned by thread '" + owner.getName()
                    + "' but was accessed from '" + current.getName()
                    + "'. This buffer is a write-through cache over the core store and must only be"
                    + " touched by the Kafka Streams thread; see the class javadoc.");
        }
    }

    private static boolean assertionsEnabled() {
        boolean enabled = false;
        assert enabled = true;
        return enabled;
    }
}
