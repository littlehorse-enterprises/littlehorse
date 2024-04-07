package io.littlehorse.canary.prometheus;

import com.google.common.util.concurrent.AtomicDouble;
import io.littlehorse.canary.proto.Metric;
import io.littlehorse.canary.proto.MetricKey;
import io.littlehorse.canary.util.Shutdown;
import io.micrometer.core.instrument.*;
import io.micrometer.core.instrument.binder.MeterBinder;
import java.util.*;
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
    private final Map<MetricKey, PrometheusMetric> currentMeters;

    public PrometheusMetricStoreExporter(final KafkaStreams kafkaStreams, final String storeName) {
        this.kafkaStreams = kafkaStreams;
        this.storeName = storeName;
        currentMeters = new HashMap<>();
    }

    private static List<Tag> toTags(final MetricKey key) {
        return key.getTagsList().stream()
                .map(tag -> Tag.of(tag.getKey(), tag.getValue()))
                .toList();
    }

    @Override
    public void bindTo(final MeterRegistry registry) {
        final ScheduledExecutorService mainExecutor = Executors.newSingleThreadScheduledExecutor();
        Shutdown.addShutdownHook("Latency Metrics Exporter", () -> {
            mainExecutor.shutdownNow();
            mainExecutor.awaitTermination(1, TimeUnit.SECONDS);
        });
        mainExecutor.scheduleAtFixedRate(() -> updateMetrics(registry), 30, 30, TimeUnit.SECONDS);
    }

    private void updateMetrics(final MeterRegistry registry) {
        log.trace("Exporting metrics");

        if (!kafkaStreams.state().equals(KafkaStreams.State.RUNNING)) {
            log.warn("It was not possible to export metrics because kafka streams is not running");
            return;
        }

        final ReadOnlyKeyValueStore<MetricKey, Metric> store = kafkaStreams.store(
                StoreQueryParameters.fromNameAndType(storeName, QueryableStoreTypes.keyValueStore()));

        final Set<MetricKey> foundMetrics = new HashSet<>();

        try (KeyValueIterator<MetricKey, Metric> records = store.all()) {
            while (records.hasNext()) {
                final KeyValue<MetricKey, Metric> record = records.next();
                foundMetrics.add(record.key);

                final PrometheusMetric current = currentMeters.get(record.key);
                if (current == null) {
                    final AtomicDouble newMeter = new AtomicDouble(record.value.getValue());
                    final Meter.Id meterId = Gauge.builder(record.key.getId(), newMeter, AtomicDouble::get)
                            .tags(toTags(record.key))
                            .register(registry)
                            .getId();
                    currentMeters.put(record.key, new PrometheusMetric(meterId, newMeter));
                } else {
                    current.meter.set(record.value.getValue());
                }
            }
        }

        final Set<MetricKey> currentMetricKeys = currentMeters.keySet();
        if (!currentMetricKeys.equals(foundMetrics)) {
            final Set<MetricKey> metricsToRemove = currentMetricKeys.stream()
                    .filter(metricKey -> !foundMetrics.contains(metricKey))
                    .collect(Collectors.toSet());

            for (MetricKey metricToRemove : metricsToRemove) {
                final PrometheusMetric prometheusMetric = currentMeters.remove(metricToRemove);
                final boolean wasRemovedFromRegistry = registry.remove(prometheusMetric.id) != null;

                if (wasRemovedFromRegistry) {
                    log.debug("Metric {} removed", metricToRemove.getId());
                } else {
                    log.warn(
                            "It was not possible to remove metric '{}', not present at the MeterRegistry",
                            metricToRemove.getId());
                }
            }
        }
    }

    record PrometheusMetric(Meter.Id id, AtomicDouble meter) {}
}
