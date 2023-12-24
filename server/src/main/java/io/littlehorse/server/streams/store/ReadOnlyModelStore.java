package io.littlehorse.server.streams.store;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.Storeable;
import io.littlehorse.common.model.AbstractGetable;
import io.littlehorse.common.model.getable.ObjectIdModel;

/**
 * TODO: Eduwer, what is this used for?
 */
@Deprecated(forRemoval = true)
public interface ReadOnlyModelStore {

    String DEFAULT_TENANT = "default";

    <U extends Message, T extends Storeable<U>> T get(String storeableKey, Class<T> cls);

    @SuppressWarnings("unchecked")
    default <U extends Message, T extends AbstractGetable<U>> StoredGetable<U, T> get(ObjectIdModel<?, U, T> id) {
        String key = id.getType().getNumber() + "/";
        key += id.toString();
        return (StoredGetable<U, T>) get(key, StoredGetable.class);
    }

    <T extends Storeable<?>> LHKeyValueIterator<T> prefixScan(String prefix, Class<T> cls);

    default <U extends Message, T extends Storeable<U>> T getLastFromPrefix(String prefix, Class<T> cls) {
        try (LHKeyValueIterator<T> iterator = reversePrefixScan(prefix, cls)) {
            if (iterator.hasNext()) {
                return iterator.next().getValue();
            } else {
                return null;
            }
        }
    }

    <T extends Storeable<?>> LHKeyValueIterator<T> reversePrefixScan(String prefix, Class<T> cls);

    <T extends Storeable<?>> LHKeyValueIterator<T> range(String start, String end, Class<T> cls);

    <T extends LHSerializable<?>> LHIterator<T> iterate(String start, String end, Class<T> cls);
}
