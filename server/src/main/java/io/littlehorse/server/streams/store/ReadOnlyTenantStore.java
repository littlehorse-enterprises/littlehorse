package io.littlehorse.server.streams.store;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.Storeable;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import lombok.Getter;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;

/*
 * This is a wrapper around rocksdb stores for a single partition. It simply handles
 * serialization and segregation of types. This allows us to open only one
 * State Store and store multiple types of stuff within it, which allows for an
 * equivalent programming experience to just creating multiple KeyValueStore's
 * with different value types using Kafka Streams' standard API.
 *
 * However, the problem with multiple stores is that each store gets its own
 * changelog topic with many partitions, and each partition means there is another
 * RocksDB instance opened up. Those two facts mean that more stores -->
 * - More load on the Kafka brokers due to additional partitions.
 * - Slower startup time because more RocksDB's must be opened.
 * - More TopicPartition's involved in each Transaction, which hurts performance
 *   slightly.
 * The performance and stability benefits
 * of consolidating into one state store far outweigh the extra code written
 * in this directory.
 *
 * Lastly, having a single point of entry for all reads/puts to RocksDB makes the
 * implementation of Multi-Tenancy much easier: just put a prefix on all keys
 * for a given namespace.
 */
public class ReadOnlyTenantStore implements ReadOnlyModelStore {

    @Getter
    public final String tenantId;

    private final SerdeReadOnlyModelStore serdeModelStore;

    public ReadOnlyTenantStore(
            ReadOnlyKeyValueStore<String, Bytes> nativeStore, String tenantId, ExecutionContext executionContext) {
        this.tenantId = tenantId;
        this.serdeModelStore = new SerdeReadOnlyModelStore(nativeStore, executionContext);
    }

    @Override
    public <U extends Message, T extends Storeable<U>> T get(String storeKey, Class<T> cls) {
        String keyToLookFor = appendTenantPrefixTo(Storeable.getFullStoreKey(cls, storeKey));
        return serdeModelStore.get(keyToLookFor, cls);
    }

    /**
     * Make sure to `.close()` the result!
     */
    public <T extends Storeable<?>> LHKeyValueIterator<T> prefixScan(String key, Class<T> cls) {
        return serdeModelStore.prefixScan(appendTenantPrefixTo(Storeable.getFullStoreKey(cls, key)), cls);
    }

    public <T extends Storeable<?>> LHKeyValueIterator<T> reversePrefixScan(String prefix, Class<T> cls) {
        String start = Storeable.getFullStoreKey(cls, prefix);
        // The Streams ReadOnlyKeyValueStore doesn't have a reverse prefix scan.
        // However, they do have a reverse range scan. So we take the prefix and
        // then we use the fact that we know the next character after the prefix is
        // one of [a-bA-B0-9\/], so we just need to append an Ascii character
        // greater than Z. We'll go with the '~', which is the greatest Ascii
        // character.
        String end = start + '~';
        return serdeModelStore.reversePrefixScan(appendTenantPrefixTo(start), appendTenantPrefixTo(end), cls);
    }

    /**
     * Does a range scan over the provided object id's (note: these are NOT full
     * store keys.)
     *
     * @param <T>   type of object
     * @param start start object id
     * @param end   end object id
     * @param cls   type
     * @return an iter
     */
    public <T extends Storeable<?>> LHKeyValueIterator<T> range(String start, String end, Class<T> cls) {
        return serdeModelStore.range(
                appendTenantPrefixTo(Storeable.getFullStoreKey(cls, start)),
                appendTenantPrefixTo(Storeable.getFullStoreKey(cls, end)),
                cls);
    }

    @Override
    public <T extends LHSerializable<?>> LHIterator<T> iterate(String start, String end, Class<T> cls) {
        return serdeModelStore.iterate(start, end, cls);
    }

    @Override
    public <T extends LHSerializable<?>> LHIterator<T> iterate(String start, String end, Class<T> cls) {
        return serdeModelStore.iterate(start, end, cls);
    }

    protected String appendTenantPrefixTo(String key) {
        return tenantId + "/" + key;
    }
}
