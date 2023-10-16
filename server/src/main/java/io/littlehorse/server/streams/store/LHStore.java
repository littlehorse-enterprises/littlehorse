package io.littlehorse.server.streams.store;

import io.littlehorse.common.Storeable;
import io.littlehorse.common.proto.StoreableType;

public interface LHStore extends ReadOnlyLHStore {

    void delete(Storeable<?> thing);

    void put(Storeable<?> thing);

    void delete(String storeKey, StoreableType cls);
}
