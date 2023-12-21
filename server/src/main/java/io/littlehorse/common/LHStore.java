package io.littlehorse.common;

import static io.littlehorse.server.streams.ServerTopology.*;

import io.littlehorse.common.proto.LHStoreType;
import lombok.Getter;

@Getter
public enum LHStore {
    CORE(CORE_STORE),
    GLOBAL_METADATA(GLOBAL_METADATA_STORE),
    REPARTITION(CORE_REPARTITION_STORE);

    private final String storeName;

    LHStore(String storeName) {
        this.storeName = storeName;
    }

    LHStore(LHStoreType protoStoreType) {
        switch (protoStoreType) {
            case CORE:
                this.storeName = CORE_STORE;
                return;
            case REPARTITION:
                this.storeName = CORE_REPARTITION_STORE;
                return;
            case METADATA:
                this.storeName = GLOBAL_METADATA_STORE;
                return;
            case UNRECOGNIZED:
        }
        throw new IllegalArgumentException("Invalid store type %s".formatted(protoStoreType));
    }
}
