package io.littlehorse.server.monitoring;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import java.io.Closeable;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class StandbyMetrics implements MeterBinder, Closeable {
    private final String METRIC_NAME = "kafka_streams_lag_standby_store";
    private final String STANDBY_PARTITIONS_METRIC_NAME = "standby_partitions";
    private final String STORE_NAME = "store_name";
    private final String INSTANCE_ID = "instance_id";
    private final Map<String, StandbyStoresOnInstance> standbyStores;
    private MeterRegistry registry;
    private final ScheduledExecutorService mainExecutor;
    private final String instanceId;
    private final long writeBufferManagerSize;

    public StandbyMetrics(
            final Map<String, StandbyStoresOnInstance> standbyStores, String instanceId, long writeBufferManagerSize) {
        this.standbyStores = standbyStores;
        mainExecutor = Executors.newSingleThreadScheduledExecutor();
        this.instanceId = instanceId;
        this.writeBufferManagerSize = writeBufferManagerSize;
    }

    @Override
    public void bindTo(MeterRegistry registry) {
        this.registry = registry;
        mainExecutor.scheduleAtFixedRate(this::registerMetrics, 1, 1, TimeUnit.SECONDS);
    }

    void registerMetrics() {
        // Partitions per store
        standbyStores.forEach((storeName, storeStatus) -> Gauge.builder(
                        STANDBY_PARTITIONS_METRIC_NAME, storeStatus, StandbyStoresOnInstance::registeredPartitions)
                .tag(STORE_NAME, storeName)
                .tag(INSTANCE_ID, instanceId)
                .register(registry));

        // total lag per store
        standbyStores.forEach(
                (storeName, storeStatus) -> Gauge.builder(METRIC_NAME, storeStatus, StandbyStoresOnInstance::totalLag)
                        .tag(STORE_NAME, storeName)
                        .tag(INSTANCE_ID, instanceId)
                        .register(registry));
        if (writeBufferManagerSize > 0) {
            Gauge.builder("kafka.stream.state.buffer.size.occupation.ratio", registry, reg -> {
                        double total = reg.find("kafka.stream.state.size.all.mem.tables").gauges().stream()
                                .filter(g -> "core".equals(g.getId().getTag("topology")))
                                .mapToDouble(Gauge::value)
                                .sum();
                        return total / writeBufferManagerSize;
                    })
                    .register(registry);
        }
    }

    @Override
    public void close() throws IOException {
        try {
            mainExecutor.shutdownNow();
            mainExecutor.awaitTermination(1, TimeUnit.SECONDS);
        } catch (Exception ex) {
            log.warn("Error when closing meter {}", ex.getMessage(), ex);
        }
    }
}
