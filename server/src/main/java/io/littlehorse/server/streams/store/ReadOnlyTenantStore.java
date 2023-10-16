package io.littlehorse.server.streams.store;

import com.google.protobuf.Message;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.Storeable;
import io.littlehorse.common.model.AbstractGetable;
import io.littlehorse.common.model.getable.ObjectIdModel;
import lombok.extern.slf4j.Slf4j;
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
public class ReadOnlyTenantStore extends AbstractReadOnlyLHStore implements ReadOnlyLHStore {

    protected LHServerConfig config;
    protected final String tenantId;

    // NOTE: we will pass in a Tenant ID to this in the future when we implement
    // multi-tenancy.
    public ReadOnlyTenantStore(ReadOnlyKeyValueStore<String, Bytes> rocksdb, LHServerConfig config, String tenantId) {
        super(rocksdb);
        this.config = config;
        this.tenantId = tenantId;
    }

    public <U extends Message, T extends Storeable<U>> T get(String storeableKey, Class<T> cls) {
        String fullKey = Storeable.getFullStoreKey(cls, storeableKey);
        fullKey = tenantId + "/" + fullKey;
        return super.get(fullKey, cls);
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
        return super.prefixScan(compositePrefix, cls);
    }

    public <T extends Storeable<?>> LHKeyValueIterator<T> reversePrefixScan(String prefix, Class<T> cls) {
        String start = Storeable.getFullStoreKey(cls, prefix);
        String end = tenantId + "/" + start + '~';
        start = tenantId + "/" + start;
        return reverseRange(start, end, cls);
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
        String startKey = tenantId + "/" + Storeable.getFullStoreKey(cls, start);
        String endKey = tenantId + "/" + Storeable.getFullStoreKey(cls, end);
        return super.range(startKey, endKey, cls);
    }
}
