package io.littlehorse.canary.aggregator.internal;

import static io.littlehorse.canary.aggregator.topology.LatencyTopology.LATENCY_METRICS_STORE;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.util.concurrent.AtomicDouble;
import io.littlehorse.canary.proto.MetricKey;
import io.littlehorse.canary.util.Shutdown;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.binder.MeterBinder;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;

@Slf4j
public class LatencyMetricExporter implements MeterBinder {

    private final KafkaStreams kafkaStreams;

    public LatencyMetricExporter(final KafkaStreams kafkaStreams) {
        this.kafkaStreams = kafkaStreams;
    }

    @Override
    public void bindTo(final MeterRegistry registry) {
        final Cache<MetricKey, AtomicDouble> cache = CacheBuilder.newBuilder().build();
        final ScheduledExecutorService mainExecutor = Executors.newSingleThreadScheduledExecutor();
        Shutdown.addShutdownHook("Latency Metrics Exporter", () -> {
            mainExecutor.shutdownNow();
            mainExecutor.awaitTermination(1, TimeUnit.SECONDS);
        });
        mainExecutor.scheduleAtFixedRate(() -> updateMetrics(registry, cache), 1, 1, TimeUnit.MINUTES);
    }

    private void updateMetrics(final MeterRegistry registry, final Cache<MetricKey, AtomicDouble> cache) {
        log.trace("Exporting metrics");

        if (!kafkaStreams.state().equals(KafkaStreams.State.RUNNING)) {
            log.warn("It was not possible to export metrics because kafka stream is not running");
            return;
        }

        final ReadOnlyKeyValueStore<MetricKey, Double> store = kafkaStreams.store(
                StoreQueryParameters.fromNameAndType(LATENCY_METRICS_STORE, QueryableStoreTypes.keyValueStore()));

        try (KeyValueIterator<MetricKey, Double> records = store.all()) {
            while (records.hasNext()) {
                final KeyValue<MetricKey, Double> record = records.next();
                final AtomicDouble meter = cache.getIfPresent(record.key);

                if (meter == null) {
                    final Tags tags = Tags.of(
                            "server",
                            "%s:%s".formatted(record.key.getServerHost(), record.key.getServerPort()),
                            "server_version",
                            record.key.getServerVersion());
                    final AtomicDouble newMeter =
                            registry.gauge(record.key.getId(), tags, new AtomicDouble(record.value));
                    cache.put(record.key, newMeter);
                } else {
                    meter.set(record.value);
                }
            }
        }
    }
}
