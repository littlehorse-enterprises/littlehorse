package io.littlehorse.server.streams.store;

import com.google.protobuf.Message;
import io.littlehorse.common.Storeable;
import io.littlehorse.common.model.AbstractGetable;
import io.littlehorse.common.model.getable.ObjectIdModel;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;

/**
 * TODO: Eduwer, what is this used for?
 */
public class ReadOnlyModelDefaultStore implements ReadOnlyModelStore {

    private final SerdeReadOnlyModelStore serdeModelStore;

    public ReadOnlyModelDefaultStore(
            ReadOnlyKeyValueStore<String, Bytes> nativeStore, ExecutionContext executionContext) {
        this.serdeModelStore = new SerdeReadOnlyModelStore(nativeStore, executionContext);
    }

    @Override
    public <U extends Message, T extends Storeable<U>> T get(String storeKey, Class<T> cls) {
        String fullKey = Storeable.getFullStoreKey(cls, storeKey);
        return serdeModelStore.get(fullKey, cls);
    }

    @SuppressWarnings("unchecked")
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
        return serdeModelStore.prefixScan(Storeable.getFullStoreKey(cls, fullKey), cls);
    }

    /**
     * Make sure to `.close()` the result!
     */
    public <T extends Storeable<?>> LHKeyValueIterator<T> reversePrefixScan(String prefix, Class<T> cls) {
        String start = Storeable.getFullStoreKey(cls, prefix);
        // The Streams ReadOnlyKeyValueStore doesn't have a reverse prefix scan.
        // However, they do have a reverse range scan. So we take the prefix and
        // then we use the fact that we know the next character after the prefix is
        // one of [a-bA-B0-9\/], so we just need to append an Ascii character
        // greater than Z. We'll go with the '~', which is the greatest Ascii
        // character.
        String end = start + '~';
        return serdeModelStore.reversePrefixScan(start, end, cls);
    }

    /**
     * Does a range scan over the provided object id's (note: these are NOT full
     * store keys.)
     *
     * Make sure to `.close()` the result!
     * @param <T>   type of object
     * @param start start object id
     * @param end   end object id
     * @param cls   type
     * @return an iter
     */
    public <T extends Storeable<?>> LHKeyValueIterator<T> range(String start, String end, Class<T> cls) {
        return serdeModelStore.range(Storeable.getFullStoreKey(cls, start), Storeable.getFullStoreKey(cls, end), cls);
    }
}
