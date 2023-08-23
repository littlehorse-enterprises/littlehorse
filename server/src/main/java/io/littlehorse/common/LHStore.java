package io.littlehorse.common;

import static io.littlehorse.server.streams.ServerTopology.*;

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
}
