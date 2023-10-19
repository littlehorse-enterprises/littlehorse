package io.littlehorse.server.streams.store;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.Storeable;
import io.littlehorse.common.model.AbstractGetable;
import io.littlehorse.common.model.getable.ObjectIdModel;
import io.littlehorse.sdk.common.exception.LHSerdeError;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;

public class ReadOnlyLHDefaultStore implements ReadOnlyLHStore {

    private final ReadOnlyKeyValueStore<String, Bytes> nativeStore;

    public ReadOnlyLHDefaultStore(ReadOnlyKeyValueStore<String, Bytes> nativeStore) {
        this.nativeStore = nativeStore;
    }

    @Override
    public <U extends Message, T extends Storeable<U>> T get(String storeKey, Class<T> cls) {
        String fullKey = Storeable.getFullStoreKey(cls, storeKey);
        Bytes raw = nativeStore.get(fullKey);

        if (raw == null) return null;

        try {
            return LHSerializable.fromBytes(raw.get(), cls);
        } catch (LHSerdeError exn) {
            throw new IllegalStateException("LHSerdeError indicates corrupted store.", exn);
        }
    }

    @Override
    public <U extends Message, T extends AbstractGetable<U>> StoredGetable<U, T> get(ObjectIdModel<?, U, T> id) {
        String key = id.getType().getNumber() + "/";
        key += id.toString();
        return (StoredGetable<U, T>) get(key, StoredGetable.class);
    }

    /**
     * Make sure to `.close()` the result!
     */
    public <T extends Storeable<?>> LHKeyValueIterator<T> prefixScan(String fullKey, Class<T> cls) {
        return new LHKeyValueIterator<>(
                nativeStore.prefixScan(
                        Storeable.getFullStoreKey(cls, fullKey), Serdes.String().serializer()),
                cls);
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
        return new LHKeyValueIterator<>(
                nativeStore.range(Storeable.getFullStoreKey(cls, start), Storeable.getFullStoreKey(cls, end)), cls);
    }
}
