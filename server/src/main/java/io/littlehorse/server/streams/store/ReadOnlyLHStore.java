package io.littlehorse.server.streams.store;

import com.google.protobuf.Message;
import io.littlehorse.common.Storeable;
import io.littlehorse.common.model.AbstractGetable;
import io.littlehorse.common.model.getable.ObjectIdModel;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.state.KeyValueStore;

public interface ReadOnlyLHStore {

    <U extends Message, T extends Storeable<U>> T get(String storeableKey, Class<T> cls);

    <U extends Message, T extends AbstractGetable<U>> StoredGetable<U, T> get(ObjectIdModel<?, U, T> id);

    <T extends Storeable<?>> LHKeyValueIterator<T> prefixScan(String prefix, Class<T> cls);

    <U extends Message, T extends Storeable<U>> T getLastFromPrefix(String prefix, Class<T> cls);

    <T extends Storeable<?>> LHKeyValueIterator<T> reversePrefixScan(String prefix, Class<T> cls);

    <T extends Storeable<?>> LHKeyValueIterator<T> range(String start, String end, Class<T> cls);

    static ReadOnlyLHStore defaultStore(KeyValueStore<String, Bytes> nativeStore) {
        return new LHDefaultStore(nativeStore);
    }

    static ReadOnlyLHStore defaultStore(ProcessorContext<String, ?> streamsProcessorContext, String storeName) {
        return defaultStore(streamsProcessorContext.getStateStore(storeName));
    }
}
