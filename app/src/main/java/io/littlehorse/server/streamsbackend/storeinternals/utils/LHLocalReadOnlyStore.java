package io.littlehorse.server.streamsbackend.storeinternals.utils;

import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.exceptions.LHSerdeError;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.model.Storeable;
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
 */
public class LHLocalReadOnlyStore {

    protected ReadOnlyKeyValueStore<String, Bytes> store;
    protected LHConfig config;

    public LHLocalReadOnlyStore(
        ReadOnlyKeyValueStore<String, Bytes> store,
        LHConfig config
    ) {
        this.store = store;
        this.config = config;
    }

    public <U extends MessageOrBuilder, T extends Storeable<U>> T get(
        String objectId,
        Class<T> cls
    ) {
        Bytes raw = store.get(StoreUtils.getStoreKey(objectId, cls));
        if (raw == null) {
            return null;
        }
        try {
            return LHSerializable.fromBytes(raw.get(), cls, config);
        } catch (LHSerdeError exn) {
            exn.printStackTrace();
            throw new RuntimeException(
                "Not possible to have this happen, indicates corrupted store."
            );
        }
    }

    /*
     * Make sure to `.close()` the result!
     */
    public <T extends Storeable<?>> LHKeyValueIterator<T> prefixScan(
        String prefix,
        Class<T> cls
    ) {
        String compositePrefix = StoreUtils.getStoreKey(prefix, cls);
        return new LHKeyValueIterator<>(
            store.prefixScan(compositePrefix, Serdes.String().serializer()),
            cls,
            config
        );
    }
}
