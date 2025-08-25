package io.littlehorse.server;

import com.google.protobuf.Message;
import io.littlehorse.common.Storeable;
import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import io.littlehorse.common.proto.StoreableType;
import io.littlehorse.server.streams.ServerTopology;
import io.littlehorse.server.streams.store.LHKeyValueIterator;
import io.littlehorse.server.streams.stores.TenantScopedStore;
import io.littlehorse.server.streams.topology.core.BackgroundContext;
import io.littlehorse.server.streams.util.MetadataCache;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.processor.api.MockProcessorContext;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.Stores;

public class TestTenantScopedStore implements TenantScopedStore {

    private final TenantScopedStore delegated;

    public TestTenantScopedStore() {
        MockProcessorContext<String, Bytes> context = new MockProcessorContext<>();
        KeyValueStore<String, Bytes> inMemoryStore = Stores.keyValueStoreBuilder(
                        Stores.inMemoryKeyValueStore(ServerTopology.CORE_STORE), Serdes.String(), Serdes.Bytes())
                .withLoggingDisabled()
                .build();
        inMemoryStore.init(context.getStateStoreContext(), inMemoryStore);
        this.delegated = TenantScopedStore.newInstance(
                context.getStateStore(ServerTopology.CORE_STORE), new TenantIdModel("test"), new BackgroundContext());
    }

    @Override
    public void put(Storeable<?> thing) {
        delegated.put(thing);
    }

    @Override
    public void delete(String storeKey, StoreableType type) {
        delegated.delete(storeKey, type);
    }

    @Override
    public <U extends Message, T extends Storeable<U>> T get(String storeKey, Class<T> cls) {
        return delegated.get(storeKey, cls);
    }

    @Override
    public <T extends Storeable<?>> LHKeyValueIterator<T> prefixScan(String key, Class<T> cls) {
        return delegated.prefixScan(key, cls);
    }

    @Override
    public <T extends Storeable<?>> LHKeyValueIterator<T> reversePrefixScan(String prefix, Class<T> cls) {
        return delegated.reversePrefixScan(prefix, cls);
    }

    @Override
    public <T extends Storeable<?>> LHKeyValueIterator<T> range(String start, String end, Class<T> cls) {
        return delegated.range(start, end, cls);
    }

    @Override
    public <T extends Storeable<?>> T getLastFromPrefix(String prefix, Class<T> cls) {
        return delegated.getLastFromPrefix(prefix, cls);
    }

    @Override
    public void enableCache(MetadataCache metadataCache) {
        delegated.enableCache(metadataCache);
    }
}
