package io.littlehorse.canary.aggregator.internal;

import com.google.common.util.concurrent.AtomicDouble;
import io.littlehorse.canary.proto.MetricKey;
import io.littlehorse.canary.proto.MetricValue;
import io.littlehorse.canary.util.ShutdownHook;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.binder.MeterBinder;
import java.time.Duration;
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
public class MetricStoreExporter implements MeterBinder, AutoCloseable {

    private final KafkaStreams kafkaStreams;
    private final String storeName;
    private final Map<MetricKey, PrometheusMetric> currentMeters = new HashMap<>();
    private final Duration refreshPeriod;
    private ScheduledExecutorService mainExecutor;

    public MetricStoreExporter(final KafkaStreams kafkaStreams, final String storeName, final Duration refreshPeriod) {
        this.kafkaStreams = kafkaStreams;
        this.storeName = storeName;
        this.refreshPeriod = refreshPeriod;
    }

    private static List<Tag> toTags(final MetricKey key) {
        final List<Tag> tags = new ArrayList<>();
        tags.add(Tag.of("server", "%s:%s".formatted(key.getServerHost(), key.getServerPort())));
        tags.add(Tag.of("server_version", key.getServerVersion()));
        tags.addAll(key.getTagsList().stream()
                .map(tag -> Tag.of(tag.getKey(), tag.getValue()))
                .toList());
        return tags;
    }

    @Override
    public void bindTo(final MeterRegistry registry) {
        mainExecutor = Executors.newSingleThreadScheduledExecutor();
        ShutdownHook.add("Latency Metrics Exporter", this);
        mainExecutor.scheduleAtFixedRate(
                () -> updateMetrics(registry), 0, refreshPeriod.toMillis(), TimeUnit.MILLISECONDS);
    }

    public void close() throws InterruptedException {
        mainExecutor.shutdownNow();
        mainExecutor.awaitTermination(1, TimeUnit.SECONDS);
    }

    private void updateMetrics(final MeterRegistry registry) {
        log.trace("Exporting metrics");

        if (!kafkaStreams.state().equals(KafkaStreams.State.RUNNING)) {
            log.warn("It was not possible to export metrics because kafka streams is not running");
            return;
        }

        final ReadOnlyKeyValueStore<MetricKey, MetricValue> store = kafkaStreams.store(
                StoreQueryParameters.fromNameAndType(storeName, QueryableStoreTypes.keyValueStore()));

        final Set<MetricKey> foundMetrics = new HashSet<>();

        try (KeyValueIterator<MetricKey, MetricValue> records = store.all()) {
            while (records.hasNext()) {
                final KeyValue<MetricKey, MetricValue> record = records.next();
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
