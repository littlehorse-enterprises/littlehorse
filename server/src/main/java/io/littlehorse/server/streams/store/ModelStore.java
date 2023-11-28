package io.littlehorse.server.streams.store;

import io.littlehorse.common.LHConstants;
import io.littlehorse.common.Storeable;
import io.littlehorse.common.proto.StoreableType;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.Objects;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;

public interface ModelStore extends ReadOnlyModelStore {

    default void delete(Storeable<?> thing) {
        this.delete(thing.getStoreKey(), thing.getType());
    }

    default void put(Storeable<?> storeable) {
        put(storeable.getFullStoreKey(), storeable);
    }

    void put(String storeKey, Storeable<?> storeable);

    void delete(String storeKey, StoreableType cls);

    static DefaultModelStore defaultStore(
            KeyValueStore<String, Bytes> keyValueStore, ExecutionContext executionContext) {
        return new DefaultModelStore(keyValueStore, executionContext);
    }

    static ReadOnlyModelDefaultStore defaultStore(
            ReadOnlyKeyValueStore<String, Bytes> keyValueStore, ExecutionContext executionContext) {
        return new ReadOnlyModelDefaultStore(keyValueStore, executionContext);
    }

    static ModelStore instanceFor(
            KeyValueStore<String, Bytes> nativeStore, String tenantId, ExecutionContext executionContext) {
        if (Objects.equals(tenantId, LHConstants.DEFAULT_TENANT)) {
            return ModelStore.defaultStore(nativeStore, executionContext);
        } else {
            return new TenantModelStore(nativeStore, tenantId, executionContext);
        }
    }

    static TenantModelStore tenantStoreFor(
            KeyValueStore<String, Bytes> nativeStore, String tenantId, ExecutionContext executionContext) {
        if (tenantId != null && !tenantId.equals(LHConstants.DEFAULT_TENANT)) {
            return new TenantModelStore(nativeStore, tenantId, executionContext);
        } else {
            return null;
        }
    }

    static ReadOnlyTenantStore tenantStoreFor(
            ReadOnlyKeyValueStore<String, Bytes> nativeStore, String tenantId, ExecutionContext executionContext) {
        if (tenantId != null && !tenantId.equals(LHConstants.DEFAULT_TENANT)) {
            return new ReadOnlyTenantStore(nativeStore, tenantId, executionContext);
        } else {
            return null;
        }
    }

    static ReadOnlyModelStore instanceFor(
            ReadOnlyKeyValueStore<String, Bytes> nativeStore, String tenantId, ExecutionContext executionContext) {
        if (Objects.equals(tenantId, LHConstants.DEFAULT_TENANT)) {
            return ModelStore.defaultStore(nativeStore, executionContext);
        } else {
            return new ReadOnlyTenantStore(nativeStore, tenantId, executionContext);
        }
    }
}
