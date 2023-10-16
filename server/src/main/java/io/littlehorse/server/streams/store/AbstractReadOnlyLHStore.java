package io.littlehorse.server.streams.store;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.Storeable;
import io.littlehorse.sdk.common.exception.LHSerdeError;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;

@Slf4j
abstract class AbstractReadOnlyLHStore implements ReadOnlyLHStore {

    private final ReadOnlyKeyValueStore<String, Bytes> nativeStore;

    protected AbstractReadOnlyLHStore(ReadOnlyKeyValueStore<String, Bytes> nativeStore) {
        this.nativeStore = nativeStore;
    }

    public <U extends Message, T extends Storeable<U>> T get(String fullKey, Class<T> cls) {
        log.trace("Getting {} from rocksdb", fullKey);
        Bytes raw = nativeStore.get(fullKey);

        if (raw == null) return null;

        try {
            return LHSerializable.fromBytes(raw.get(), cls);
        } catch (LHSerdeError exn) {
            throw new IllegalStateException("LHSerdeError indicates corrupted store.", exn);
        }
    }

    /**
     * Make sure to `.close()` the result!
     */
    public <T extends Storeable<?>> LHKeyValueIterator<T> prefixScan(String fullKey, Class<T> cls) {
        return new LHKeyValueIterator<>(
                nativeStore.prefixScan(fullKey, Serdes.String().serializer()), cls);
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
        return new LHKeyValueIterator<>(nativeStore.reverseRange(start, end), cls);
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

    protected <T extends Storeable<?>> LHKeyValueIterator<T> reverseRange(String start, String end, Class<T> cls) {
        return new LHKeyValueIterator<>(nativeStore.reverseRange(start, end), cls);
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
        return new LHKeyValueIterator<>(nativeStore.range(start, end), cls);
    }
}
