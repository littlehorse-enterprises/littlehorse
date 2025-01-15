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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
public class MetricStoreExporter implements MeterBinder, AutoCloseable {

    private final KafkaStreams kafkaStreams;
    private final String storeName;
    private final Map<PrometheusMetric, CachedMeter> currentMeters = new HashMap<>();
    private final Duration refreshPeriod;
    private ScheduledExecutorService mainExecutor;

    public MetricStoreExporter(final KafkaStreams kafkaStreams, final String storeName, final Duration refreshPeriod) {
        this.kafkaStreams = kafkaStreams;
        this.storeName = storeName;
        this.refreshPeriod = refreshPeriod;
    }

    private static List<Tag> toMetricTags(final MetricKey key) {
        final List<Tag> tags = new ArrayList<>();
        tags.add(Tag.of("server", "%s:%s".formatted(key.getServerHost(), key.getServerPort())));
        tags.add(Tag.of("server_version", key.getServerVersion()));
        tags.addAll(key.getTagsList().stream()
                .map(tag -> Tag.of(tag.getKey(), tag.getValue()))
                .toList());
        return tags;
    }

    private static String toMetricId(final MetricKey key, final String suffix) {
        return "%s_%s".formatted(key.getId(), suffix);
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

        final Set<PrometheusMetric> foundMetrics = new HashSet<>();

        try (KeyValueIterator<MetricKey, MetricValue> records = store.all()) {
            while (records.hasNext()) {
                final KeyValue<MetricKey, MetricValue> record = records.next();

                record.value.getValuesMap().entrySet().stream()
                        .map(entry -> new PrometheusMetric(
                                toMetricId(record.key, entry.getKey()), toMetricTags(record.key), entry.getValue()))
                        .forEach(metric -> {
                            foundMetrics.add(metric);
                            final CachedMeter current = currentMeters.get(metric);

                            if (current == null) {
                                final AtomicDouble newMeter = new AtomicDouble(metric.value);
                                final Meter.Id meterId = Gauge.builder(metric.id, newMeter, AtomicDouble::get)
                                        .tags(metric.tags)
                                        .register(registry)
                                        .getId();
                                currentMeters.put(metric, new CachedMeter(meterId, newMeter));
                            } else {
                                current.meter.set(metric.value);
                            }
                        });
            }
        }

        currentMeters.keySet().stream()
                .filter(metricKey -> !foundMetrics.contains(metricKey))
                .forEach(metricKey -> {
                    final CachedMeter prometheusMetric = currentMeters.remove(metricKey);
                    final boolean wasRemovedFromRegistry = registry.remove(prometheusMetric.id) != null;

                    if (wasRemovedFromRegistry) {
                        log.debug("Metric {} removed", metricKey);
                    } else {
                        log.warn(
                                "It was not possible to remove metric '{}', not present at the MeterRegistry",
                                metricKey);
                    }
                });
    }

    record CachedMeter(Meter.Id id, AtomicDouble meter) {}

    record PrometheusMetric(String id, List<Tag> tags, Double value) {}
}
