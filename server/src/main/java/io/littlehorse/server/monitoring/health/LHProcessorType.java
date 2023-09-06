package io.littlehorse.server.monitoring.health;

import lombok.Getter;

@Getter
public enum LHProcessorType {
    CORE("core"),
    REPARTITION("repartition"),
    GLOBAL_METADATA("global-metadata"),
    METADATA("metadata"),
    TIMER("timer");

    private final String storeName;

    LHProcessorType(String storeName) {
        this.storeName = storeName;
    }
}
