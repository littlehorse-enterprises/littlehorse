package io.littlehorse.server.streams.topology.core;

import io.littlehorse.common.model.getable.objectId.ExternalEventDefIdModel;
import io.littlehorse.server.streams.storeinternals.EventCorrelationMarkerModel;
import io.littlehorse.server.streams.stores.TenantScopedStore;

public class CorrelationMarkerManager {

    private final TenantScopedStore store;

    public CorrelationMarkerManager(TenantScopedStore store) {
        this.store = store;
    }

    public EventCorrelationMarkerModel getMarker(String key, ExternalEventDefIdModel externalEventDefId) {
        String storeKey = EventCorrelationMarkerModel.getStoreKey(key, externalEventDefId);

        EventCorrelationMarkerModel result = store.get(storeKey, EventCorrelationMarkerModel.class);
        if (result == null) {
            result = new EventCorrelationMarkerModel(key, externalEventDefId);
        }
        return result;
    }

    public void saveCorrelationMarker(EventCorrelationMarkerModel toSave) {
        if (toSave.isEmpty()) {
            store.delete(toSave);
        } else {
            store.put(toSave);
        }
    }
}
