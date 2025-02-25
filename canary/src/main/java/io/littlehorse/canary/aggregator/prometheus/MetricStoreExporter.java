package io.littlehorse.canary.aggregator.prometheus;

import com.google.common.util.concurrent.AtomicDouble;
import io.littlehorse.canary.infra.ShutdownHook;
import io.littlehorse.canary.proto.MetricKey;
import io.littlehorse.canary.proto.MetricValue;
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
    private final Map<PrometheusMetric, CachedMeter> cachedMeters = new HashMap<>();
    private final Duration frequency;
    private ScheduledExecutorService mainExecutor;

    public MetricStoreExporter(final KafkaStreams kafkaStreams, final String storeName, final Duration frequency) {
        this.kafkaStreams = kafkaStreams;
        this.storeName = storeName;
        this.frequency = frequency;
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
        return "%s_%s".formatted(key.getName(), suffix);
    }

    @Override
    public void bindTo(final MeterRegistry registry) {
        mainExecutor = Executors.newSingleThreadScheduledExecutor();
        ShutdownHook.add("Latency Metrics Exporter", this);
        mainExecutor.scheduleAtFixedRate(
                () -> {
                    try {
                        updateMetrics(registry);
                    } catch (Exception e) {
                        log.error("Error when exporting metrics", e);
                    }
                },
                0,
                frequency.toMillis(),
                TimeUnit.MILLISECONDS);
    }

    public void close() throws InterruptedException {
        mainExecutor.shutdownNow();
        mainExecutor.awaitTermination(1, TimeUnit.SECONDS);
    }

    private void updateMetrics(final MeterRegistry registry) {
        log.debug("Exporting metrics");

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
                        .map(entry -> PrometheusMetric.builder()
                                .id(toMetricId(record.key, entry.getKey()))
                                .tags(toMetricTags(record.key))
                                .value(entry.getValue())
                                .build())
                        .forEach(metric -> {
                            foundMetrics.add(metric);
                            final CachedMeter cachedMeter = cachedMeters.get(metric);

                            if (cachedMeter == null) {
                                final AtomicDouble newMeter = new AtomicDouble(metric.getValue());
                                final Meter.Id meterId = Gauge.builder(metric.getId(), newMeter, AtomicDouble::get)
                                        .tags(metric.getTags())
                                        .register(registry)
                                        .getId();
                                cachedMeters.put(metric, new CachedMeter(meterId, newMeter));
                                log.info("Metric {} added", metric);
                            } else {
                                log.debug("Updating existing metric {}", metric.getId());
                                cachedMeter.getMeter().set(metric.getValue());
                            }
                        });
            }
        }

        final List<PrometheusMetric> metricsToRemove = cachedMeters.keySet().stream()
                .filter(metricKey -> !foundMetrics.contains(metricKey))
                .toList();
        // to avoid ConcurrentModificationException
        metricsToRemove.forEach(metricKey -> {
            final CachedMeter cachedMeter = cachedMeters.remove(metricKey);
            final boolean wasRemovedFromRegistry = registry.remove(cachedMeter.getId()) != null;

            if (wasRemovedFromRegistry) {
                log.info("Metric {} removed", metricKey);
            } else {
                log.warn("It was not possible to remove metric '{}', not present at the MeterRegistry", metricKey);
            }
        });
    }
}
