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

class SerdeReadOnlyModelStore implements ReadOnlyModelStore {

    private final ReadOnlyKeyValueStore<String, Bytes> nativeStore;

    public SerdeReadOnlyModelStore(final ReadOnlyKeyValueStore<String, Bytes> nativeStore) {
        if (nativeStore == null) {
            throw new NullPointerException();
        }
        this.nativeStore = nativeStore;
    }

    @Override
    public <U extends Message, T extends Storeable<U>> T get(String fullKey, Class<T> cls) {
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
        throw new UnsupportedOperationException();
    }

    @Override
    public <T extends Storeable<?>> LHKeyValueIterator<T> prefixScan(String fullKey, Class<T> cls) {
        return new LHKeyValueIterator<>(
                nativeStore.prefixScan(fullKey, Serdes.String().serializer()), cls);
    }

    @Override
    public <T extends Storeable<?>> LHKeyValueIterator<T> reversePrefixScan(String prefix, Class<T> cls) {
        throw new UnsupportedOperationException();
    }

    public <T extends Storeable<?>> LHKeyValueIterator<T> reversePrefixScan(
            String startKey, String endKey, Class<T> cls) {
        return new LHKeyValueIterator<>(nativeStore.reverseRange(startKey, endKey), cls);
    }

    @Override
    public <T extends Storeable<?>> LHKeyValueIterator<T> range(String start, String end, Class<T> cls) {
        return new LHKeyValueIterator<>(nativeStore.range(start, end), cls);
    }
}
