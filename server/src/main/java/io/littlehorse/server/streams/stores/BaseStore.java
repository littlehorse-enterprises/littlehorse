package io.littlehorse.server.streams.stores;

import io.littlehorse.common.Storeable;
import io.littlehorse.common.model.getable.objectId.WfRunIdModel;
import io.littlehorse.common.proto.StoreableType;
import io.littlehorse.server.streams.storeinternals.WfRunStoredInventoryModel;

interface BaseStore extends ReadOnlyBaseStore {

    void put(Storeable<?> thing);

    void delete(String storeKey, StoreableType type);

    /**
     * To be removed in version 2.0. See Proposal #9
     */
    WfRunStoredInventoryModel getWfRunInventoryFromLegacyKey(WfRunIdModel wfRunId);

    default void delete(Storeable<?> thing) {
        delete(thing.getStoreKey(), thing.getType());
    }
}
