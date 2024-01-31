package io.littlehorse.server.streams.stores;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.Storeable;
import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import io.littlehorse.sdk.common.exception.LHSerdeError;
import io.littlehorse.server.streams.store.LHKeyValueIterator;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import lombok.Getter;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;

/**
 * Package-private class used to implement multiple logical stores within a single Kafka Streams
 * KeyValueStore. This is motivated purely for performance reasons because adding multiple stores
 * leads to a proliferation of open files (RocksDB), many changelog partitions, slower rebalances,
 * more partitions per Streams Transaction (longer commits), and slower flushes.
 *
 * In addition to allowing different Storeable's to be stored in logically separated stores, this
 * class runs in two modes: Cluster-scoped, or Tenant-scoped. In the Tenant-Scoped mode, a prefix
 * is pre-pended to every key so that we can logically isolate Tenant-Scoped data.
 */
abstract class ReadOnlyBaseStoreImpl implements ReadOnlyBaseStore {

    @Getter
    protected final TenantIdModel tenantId;

    protected final ExecutionContext executionContext;
    private final ReadOnlyKeyValueStore<String, Bytes> nativeStore;

    ReadOnlyBaseStoreImpl(
            ReadOnlyKeyValueStore<String, Bytes> nativeStore,
            TenantIdModel tenantId,
            ExecutionContext executionContext) {

        if (nativeStore == null) {
            throw new NullPointerException();
        }
        this.tenantId = tenantId;
        this.nativeStore = nativeStore;
        this.executionContext = executionContext;
    }

    ReadOnlyBaseStoreImpl(ReadOnlyKeyValueStore<String, Bytes> nativeStore, ExecutionContext executionContext) {
        this(nativeStore, null, executionContext);
    }

    @Override
    public <U extends Message, T extends Storeable<U>> T get(String storeKey, Class<T> cls) {
        String keyToLookFor = maybeAddTenantPrefix(Storeable.getFullStoreKey(cls, storeKey));
        Bytes raw = nativeStore.get(keyToLookFor);

        if (raw == null) return null;

        try {
            return LHSerializable.fromBytes(raw.get(), cls, executionContext);
        } catch (LHSerdeError exn) {
            throw new IllegalStateException("LHSerdeError indicates corrupted store.", exn);
        }
    }

    /**
     * The LHKeyValueIterator *MUST* be `.close()`'ed, or we will have a resource leak.
     * There is an incoming KIP in Kafka that allows metrics to track leaked RocksDB iterators,
     * once it is implemented, we should track that metric closely.
     */
    @Override
    public <T extends Storeable<?>> LHKeyValueIterator<T> prefixScan(String key, Class<T> cls) {
        String actualPrefix = maybeAddTenantPrefix(Storeable.getFullStoreKey(cls, key));

        return new LHKeyValueIterator<>(
                nativeStore.prefixScan(actualPrefix, Serdes.String().serializer()), cls, executionContext);
    }

    /**
     * The LHKeyValueIterator *MUST* be `.close()`'ed, or we will have a resource leak.
     * There is an incoming KIP in Kafka that allows metrics to track leaked RocksDB iterators,
     * once it is implemented, we should track that metric closely.
     */
    @Override
    public <T extends Storeable<?>> LHKeyValueIterator<T> reversePrefixScan(String prefix, Class<T> cls) {
        // The Streams ReadOnlyKeyValueStore doesn't have a reverse prefix scan.
        // However, they do have a reverse range scan. So we take the prefix and
        // then we use the fact that we know the next character after the prefix is
        // one of [a-bA-B0-9\/], so we just need to append an Ascii character
        // greater than Z. We'll go with the '~', which is the greatest Ascii
        // character.
        String start = maybeAddTenantPrefix(Storeable.getFullStoreKey(cls, prefix));
        String end = start + '~';

        return new LHKeyValueIterator<>(nativeStore.reverseRange(start, end), cls, executionContext);
    }

    /**
     * Does a range scan over the provided Storeable Keys. These are NOT Getable object Id's.
     * Note that you MUST call `.close()` on the result to prevent resource leaks.
     * @param <T>   type of Storeable Object
     * @param start start Storeable Key
     * @param end   end Storeable Key
     * @param cls   Storeable Type
     * @return an iter
     */
    @Override
    public <T extends Storeable<?>> LHKeyValueIterator<T> range(String start, String end, Class<T> cls) {
        start = maybeAddTenantPrefix(Storeable.getFullStoreKey(cls, start));
        end = maybeAddTenantPrefix(Storeable.getFullStoreKey(cls, end));

        return new LHKeyValueIterator<>(nativeStore.range(start, end), cls, executionContext);
    }

    @Override
    public <T extends Storeable<?>> T getLastFromPrefix(String prefix, Class<T> cls) {
        try (LHKeyValueIterator<T> iterator = reversePrefixScan(prefix, cls)) {
            if (iterator.hasNext()) {
                return iterator.next().getValue();
            } else {
                return null;
            }
        }
    }

    protected String maybeAddTenantPrefix(String key) {
        return tenantId == null ? key : tenantId.toString() + "/" + key;
    }
}
