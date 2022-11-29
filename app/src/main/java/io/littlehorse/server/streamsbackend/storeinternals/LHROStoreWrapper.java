package io.littlehorse.server.streamsbackend.storeinternals;

import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.exceptions.LHSerdeError;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.model.Storeable;
import io.littlehorse.server.streamsbackend.storeinternals.utils.LHKeyValueIterator;
import io.littlehorse.server.streamsbackend.storeinternals.utils.StoreUtils;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.state.KeyValueIterator;
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
public class LHROStoreWrapper {

    protected ReadOnlyKeyValueStore<String, Bytes> store;
    protected LHConfig config;

    public LHROStoreWrapper(
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

    public <U extends MessageOrBuilder, T extends Storeable<U>> T getLastFromPrefix(
        String prefix,
        Class<T> cls
    ) {
        LHKeyValueIterator<T> iterator = null;
        try {
            iterator = reversePrefixScan(prefix + "/", cls);
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

    public Bytes getLastBytesFromFullPrefix(String fullPrefix) {
        KeyValueIterator<String, Bytes> rawIter = null;
        try {
            rawIter = store.reverseRange(fullPrefix + "~", fullPrefix);
            if (rawIter.hasNext()) {
                return rawIter.next().value;
            } else {
                return null;
            }
        } finally {
            if (rawIter != null) rawIter.close();
        }
    }

    public <T extends Storeable<?>> LHKeyValueIterator<T> reversePrefixScan(
        String prefix,
        Class<T> cls
    ) {
        String start = StoreUtils.getStoreKey(prefix, cls);
        // The Streams ReadOnlyKeyValueStore doesn't have a reverse prefix scan.
        // However, they do have a reverse range scan. So we take the prefix and
        // then we use the fact that we know the next character after the prefix is
        // one of [a-bA-B0-9\/], so we just need to append an Ascii character
        // greater than Z. We'll go with the '~', which is the greatest Ascii
        // character.
        String end = start + '~';
        return new LHKeyValueIterator<>(store.reverseRange(end, start), cls, config);
    }
}
