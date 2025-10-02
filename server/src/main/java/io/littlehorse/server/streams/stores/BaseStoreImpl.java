package io.littlehorse.server.streams.stores;

import io.littlehorse.common.Storeable;
import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import io.littlehorse.common.proto.StoreableType;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.state.KeyValueStore;

import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.Message;

/**
 * Package-private class that allows you to read and write storeables at either
 * the tenant or cluster scope.
 */
@Slf4j
abstract class BaseStoreImpl extends ReadOnlyBaseStoreImpl implements BaseStore {

    private final KeyValueStore<String, Bytes> nativeStore;

    BaseStoreImpl(KeyValueStore<String, Bytes> nativeStore, TenantIdModel tenantId, ExecutionContext context) {
        super(nativeStore, tenantId, context);
        this.nativeStore = nativeStore;
    }

    BaseStoreImpl(KeyValueStore<String, Bytes> nativeStore, ExecutionContext context) {
        this(nativeStore, null, context);
    }

    @Override
    public void put(Storeable<?> thing) {
        String key = maybeAddTenantPrefix(thing.getFullStoreKey());
        if (metadataCache != null) {
            metadataCache.evictCache(key);
        }
        nativeStore.put(key, new Bytes(thing.toBytes()));
    }

    /**
     * We override this method for the online migration from the legacy store format
     * to the new optimized one. For details, see Proposal #9.
     */
    @Override
    public <U extends Message, T extends Storeable<U>> T get(String storeKey, Class<T> cls) {
        String keyToLookFor = maybeAddTenantPrefix(Storeable.getFullStoreKey(cls, storeKey));
        if (metadataCache != null) {
            // Proposal #9 does not touch the stuff in the metadata cache.
            return super.get(storeKey, cls);
        } else {
            T result = super.get(storeKey, cls);
            // time to get things from the store
            GeneratedMessage stored = getFromNativeStore(keyToLookFor, cls);
            if (stored == null) {
                return null;
            }
            return LHSerializable.fromProto(stored, cls, executionContext);
        }
    }

    @Override
    public void delete(String storeKey, StoreableType type) {
        String fullKey = maybeAddTenantPrefix(Storeable.getUngroupedStoreKey(type, storeKey));
        if (metadataCache != null) {
            metadataCache.evictCache(fullKey);
        }
        nativeStore.delete(fullKey);
    }
}
