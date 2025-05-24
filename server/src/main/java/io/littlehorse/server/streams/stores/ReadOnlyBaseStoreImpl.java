package io.littlehorse.server.streams.stores;

import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.Storeable;
import io.littlehorse.common.model.MetadataGetable;
import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.server.streams.store.LHKeyValueIterator;
import io.littlehorse.server.streams.store.StoredGetable;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.util.MetadataCache;
import java.util.NoSuchElementException;
import java.util.Objects;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;

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
@Slf4j
abstract class ReadOnlyBaseStoreImpl implements ReadOnlyBaseStore {

    @Getter
    protected final TenantIdModel tenantId;

    protected final ExecutionContext executionContext;
    private final RocksDB db;
    //    private final ReadOnlyKeyValueStore<String, Bytes> nativeStore;
    protected MetadataCache metadataCache;

    ReadOnlyBaseStoreImpl(
            /*ReadOnlyKeyValueStore<String, Bytes> nativeStore,*/
            TenantIdModel tenantId, ExecutionContext executionContext, RocksDB db) {

        //        if (nativeStore == null) {
        //            throw new NullPointerException();
        //        }
        this.db = db;
        this.tenantId = tenantId;
        //        this.nativeStore = nativeStore;
        this.executionContext = executionContext;
    }

    ReadOnlyBaseStoreImpl(
            /*ReadOnlyKeyValueStore<String, Bytes> nativeStore,*/ ExecutionContext executionContext, RocksDB db) {
        this(/*nativeStore, */ null, executionContext, db);
    }

    @Override
    public <U extends Message, T extends Storeable<U>> T get(String storeKey, Class<T> cls) {
        String keyToLookFor = maybeAddTenantPrefix(Storeable.getFullStoreKey(cls, storeKey));
        if (metadataCache != null) {
            return getMetadataObject(keyToLookFor, cls);
        } else {
            // time to get things from the store
            GeneratedMessageV3 stored = getFromNativeStore(keyToLookFor, cls);
            if (stored == null) {
                return null;
            }
            return LHSerializable.fromProto(stored, cls, executionContext);
        }
    }

    private <U extends Message, T extends Storeable<U>> T getMetadataObject(String keyToLookFor, Class<T> clazz) {
        StoredGetable<? extends Message, ? extends MetadataGetable<?>> cachedGetable = metadataCache.get(keyToLookFor);
        if (cachedGetable != null) {
            return (T) cachedGetable;
        } else {
            if (metadataCache.containsKey(keyToLookFor)) {
                // we already know that the store does not contain this key
                return null;
            }
            GeneratedMessageV3 storedProto = getFromNativeStore(keyToLookFor, clazz);
            if (storedProto != null) {
                StoredGetable<U, MetadataGetable<U>> storedGetable =
                        LHSerializable.fromProto(storedProto, StoredGetable.class, executionContext);
                metadataCache.evictOrUpdate(storedGetable, keyToLookFor);
                return (T) storedGetable;
            }
            // key is not in the store, now we try to cache this missing key
            metadataCache.updateMissingKey(keyToLookFor);
        }
        return null;
    }

    @Override
    public void enableCache(MetadataCache metadataCache) {
        this.metadataCache = metadataCache;
    }

    private <U extends Message, T extends Storeable<U>> GeneratedMessageV3 getFromNativeStore(
            String keyToLookFor, Class<T> cls) {

        try {
            Bytes raw = Bytes.wrap(db.get(keyToLookFor.getBytes()));
            if (raw == null) return null;
            return LHSerializable.protoFromBytes(raw.get(), cls);
        } catch (RocksDBException e) {
            log.error("Failed to get key [{}] from rocksdb. Message: {}", keyToLookFor, e.getMessage(), e);
            throw new RuntimeException(e);
        } catch (LHSerdeException exn) {
            throw new IllegalStateException("LHSerdeError indicates corrupted store.", exn);
        }

        //        Bytes raw = nativeStore.get(keyToLookFor);
        //
        //        if (raw == null) return null;
        //
        //        try {
        //            return LHSerializable.protoFromBytes(raw.get(), cls);
        //        } catch (LHSerdeException exn) {
        //            throw new IllegalStateException("LHSerdeError indicates corrupted store.", exn);
        //        }
    }

    /**
     * The LHKeyValueIterator *MUST* be `.close()`'ed, or we will have a resource leak.
     * There is an incoming KIP in Kafka that allows metrics to track leaked RocksDB iterators,
     * once it is implemented, we should track that metric closely.
     */
    @Override
    public <T extends Storeable<?>> LHKeyValueIterator<T> prefixScan(String key, Class<T> cls) {
        String actualPrefix = maybeAddTenantPrefix(Storeable.getFullStoreKey(cls, key));
        KeyValueIterator<Bytes, byte[]> rawIter =
                pepe(actualPrefix, Serdes.String().serializer());
        return new LHKeyValueIterator<>(rawIter, cls, executionContext);
    }

    public KeyValueIterator<Bytes, byte[]> pepe(String prefix, Serializer<String> prefixKeySerializer) {
        final Bytes prefixBytes = Bytes.wrap(prefixKeySerializer.serialize(null, prefix));
        final Bytes to = incrementWithoutOverflow(prefixBytes);
        return new RocksDBRangeIterator("whatever-rocksdb-store-name", db.newIterator(), prefixBytes, to, true, false);
    }

    static Bytes incrementWithoutOverflow(final Bytes input) {
        try {
            return Bytes.increment(input);
        } catch (final IndexOutOfBoundsException e) {
            return null;
        }
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

        return new LHKeyValueIterator<>(range(start, end, false), cls, executionContext);
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
        return new LHKeyValueIterator<>(range(start, end, true), cls, executionContext);
    }

    private KeyValueIterator<Bytes, byte[]> range(final String from, final String to, boolean forward) {
        if (Objects.nonNull(from) && Objects.nonNull(to) && from.compareTo(to) > 0) {
            log.warn("Returning empty iterator for fetch with invalid key range: from > to. "
                    + "This may be due to range arguments set in the wrong order, "
                    + "or serdes that don't preserve ordering when lexicographically comparing the serialized bytes. "
                    + "Note that the built-in numerical serdes do not follow this for negative numbers");
            return new KeyValueIterator<>() {
                @Override
                public void close() {}

                @Override
                public Bytes peekNextKey() {
                    throw new NoSuchElementException();
                }

                @Override
                public boolean hasNext() {
                    return false;
                }

                @Override
                public KeyValue<Bytes, byte[]> next() {
                    throw new NoSuchElementException();
                }
            };
        }

        return new RocksDBRangeIterator(
                "whatever-rocksdb-store-name",
                db.newIterator(),
                Bytes.wrap(from.getBytes()),
                Bytes.wrap(to.getBytes()),
                forward,
                true);
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
