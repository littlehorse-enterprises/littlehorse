package io.littlehorse.server.streams.store;

import com.google.protobuf.Message;
import io.littlehorse.common.Storeable;
import io.littlehorse.common.model.AbstractGetable;
import io.littlehorse.common.model.getable.ObjectIdModel;
import java.util.Objects;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;

public interface ReadOnlyLHStore {

    public static final String DEFAULT_TENANT = "default";

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

    static ReadOnlyLHStore instanceFor(ReadOnlyKeyValueStore<String, Bytes> nativeStore, String tenantId) {
        if (Objects.equals(tenantId, "default")) {
            return new ReadOnlyLHDefaultStore(nativeStore);
        } else {
            return new ReadOnlyTenantStore(nativeStore, tenantId);
        }
    }
}
