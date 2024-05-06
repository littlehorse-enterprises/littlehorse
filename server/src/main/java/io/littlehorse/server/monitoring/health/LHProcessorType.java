package io.littlehorse.server.monitoring.health;

import lombok.Getter;

@Getter
public enum LHProcessorType {
    CORE("core-store"),
    REPARTITION("core-repartition-store"),
    GLOBAL_METADATA("global-metadata-store"),
    METADATA("metadata-store"),
    TIMER("timer-store");

    private final String storeName;

    LHProcessorType(String storeName) {
        this.storeName = storeName;
    }
}
