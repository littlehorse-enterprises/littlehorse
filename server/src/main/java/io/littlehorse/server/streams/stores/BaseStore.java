package io.littlehorse.server.streams.stores;

import io.littlehorse.common.Storeable;
import io.littlehorse.common.proto.StoreableType;

interface BaseStore extends ReadOnlyBaseStore {

    void put(Storeable<?> thing);

    void delete(String storeKey, StoreableType type);

    default void delete(Storeable<?> thing) {
        delete(thing.getStoreKey(), thing.getType());
    }
}
