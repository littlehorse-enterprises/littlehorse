package io.littlehorse.server.streams.store;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.Storeable;
import io.littlehorse.common.model.AbstractGetable;
import io.littlehorse.common.model.getable.ObjectIdModel;
import io.littlehorse.common.proto.StoreableType;
import io.littlehorse.sdk.common.exception.LHSerdeError;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serdes;
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
 * changelog topic with many partitions, and additionally each changelog topic
 * causes another Consumer Group to be created among standby tasks. Those two
 * facts mean that more stores --> much longer consumer rebalance times when
 * you have a lot of input partitions. The performance and stability benefits
 * of consolidating into one state store far outweigh the extra code written
 * in this directory.
 *
 * Lastly, having a single point of entry for all reads/puts to RocksDB make the
 * implementation of Multi-Tenancy much easier: just put a prefix on all keys
 * for a given namespace.
 */
@Slf4j
public class ReadOnlyRocksDBWrapper {

    protected ReadOnlyKeyValueStore<String, Bytes> rocksdb;
    protected LHConfig config;

    // NOTE: we will pass in a Tenant ID to this in the future when we implement
    // multi-tenancy.
    public ReadOnlyRocksDBWrapper(ReadOnlyKeyValueStore<String, Bytes> rocksdb, LHConfig config) {
        this.rocksdb = rocksdb;
        this.config = config;
    }

    public <U extends Message, T extends Storeable<U>> T get(String storeableKey, Class<T> cls) {
        String fullKey = Storeable.getFullStoreKey(cls, storeableKey);
        log.trace("Getting {} from rocksdb", fullKey);
        Bytes raw = rocksdb.get(fullKey);

        if (raw == null) return null;

        try {
            return LHSerializable.fromBytes(raw.get(), cls);
        } catch (LHSerdeError exn) {
            throw new IllegalStateException("LHSerdeError indicates corrupted store.", exn);
        }
    }

    @SuppressWarnings("unchecked")
    public <U extends Message, T extends AbstractGetable<U>> StoredGetable<U, T> get(ObjectIdModel<?, U, T> id) {
        String key = id.getType().getNumber() + "/";
        key += id.toString();
        return (StoredGetable<U, T>) get(key, StoredGetable.class);
    }

    /**
     * Make sure to `.close()` the result!
     */
    public <T extends Storeable<?>> LHKeyValueIterator<T> prefixScan(String prefix, Class<T> cls) {
        String compositePrefix = Storeable.getFullStoreKey(cls, prefix);
        return new LHKeyValueIterator<>(
                rocksdb.prefixScan(compositePrefix, Serdes.String().serializer()), cls);
    }

    public <U extends Message, T extends Storeable<U>> T getLastFromPrefix(String prefix, Class<T> cls) {

        LHKeyValueIterator<T> iterator = null;
        try {
            iterator = reversePrefixScan(prefix, cls);
            if (iterator.hasNext()) {
                return iterator.next().getValue();
            } else {
                return null;
            }
        } finally {
            if (iterator != null) {
                iterator.close();
            }
        }
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
        return new LHKeyValueIterator<>(rocksdb.reverseRange(start, end), cls);
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

        return new LHKeyValueIterator<>(
                rocksdb.range(Storeable.getFullStoreKey(cls, start), Storeable.getFullStoreKey(cls, end)), cls);
    }
}
