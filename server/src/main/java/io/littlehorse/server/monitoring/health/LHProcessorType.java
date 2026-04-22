package io.littlehorse.server.monitoring.health;

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

    public String getStoreName() {
        return this.storeName;
    }
}
