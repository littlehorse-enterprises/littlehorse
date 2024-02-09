package io.littlehorse.canary.prometheus;

import com.google.common.util.concurrent.AtomicDouble;
import io.littlehorse.canary.proto.MetricKey;
import io.littlehorse.canary.util.Shutdown;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.binder.MeterBinder;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;

@Slf4j
public class PrometheusMetricStoreExporter implements MeterBinder {

    private final KafkaStreams kafkaStreams;
    private final String storeName;
    private final Map<MetricKey, AtomicDouble> currentMeters;

    public PrometheusMetricStoreExporter(final KafkaStreams kafkaStreams, final String storeName) {
        this.kafkaStreams = kafkaStreams;
        this.storeName = storeName;
        currentMeters = new HashMap<>();
    }

    private static Tags toTags(final MetricKey key) {
        return Tags.of(
                "server",
                "%s:%s".formatted(key.getServerHost(), key.getServerPort()),
                "server_version",
                key.getServerVersion());
    }

    private static Meter.Id toMeterId(final MetricKey key) {
        return new Meter.Id(key.getId(), toTags(key), null, null, Meter.Type.GAUGE);
    }

    @Override
    public void bindTo(final MeterRegistry registry) {
        final ScheduledExecutorService mainExecutor = Executors.newSingleThreadScheduledExecutor();
        Shutdown.addShutdownHook("Latency Metrics Exporter", () -> {
            mainExecutor.shutdownNow();
            mainExecutor.awaitTermination(1, TimeUnit.SECONDS);
        });
        mainExecutor.scheduleAtFixedRate(() -> updateMetrics(registry), 30, 5, TimeUnit.SECONDS);
    }

    private void updateMetrics(final MeterRegistry registry) {
        log.trace("Exporting metrics");

        if (!kafkaStreams.state().equals(KafkaStreams.State.RUNNING)) {
            log.warn("It was not possible to export metrics because kafka streams is not running");
            return;
        }

        final ReadOnlyKeyValueStore<MetricKey, Double> store = kafkaStreams.store(
                StoreQueryParameters.fromNameAndType(storeName, QueryableStoreTypes.keyValueStore()));

        final Set<MetricKey> foundMetrics = new HashSet<>();

        try (KeyValueIterator<MetricKey, Double> records = store.all()) {
            while (records.hasNext()) {
                final KeyValue<MetricKey, Double> record = records.next();
                foundMetrics.add(record.key);

                final AtomicDouble meter = currentMeters.get(record.key);
                if (meter == null) {
                    final AtomicDouble newMeter =
                            registry.gauge(record.key.getId(), toTags(record.key), new AtomicDouble(record.value));
                    currentMeters.put(record.key, newMeter);
                } else {
                    meter.set(record.value);
                }
            }
        }

        final Set<MetricKey> currentMetricKeys = currentMeters.keySet();
        if (!currentMetricKeys.equals(foundMetrics)) {
            final Set<MetricKey> metricsToRemove = currentMetricKeys.stream()
                    .filter(metricKey -> !foundMetrics.contains(metricKey))
                    .collect(Collectors.toSet());
            for (MetricKey metricToRemove : metricsToRemove) {
                currentMeters.remove(metricToRemove);
                registry.remove(toMeterId(metricToRemove));
            }
        }
    }
}
