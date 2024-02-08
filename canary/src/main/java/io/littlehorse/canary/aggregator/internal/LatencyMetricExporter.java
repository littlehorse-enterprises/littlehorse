package io.littlehorse.canary.aggregator.internal;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.KafkaStreams;

@Slf4j
public class LatencyMetricExporter implements MeterBinder {

    private final KafkaStreams kafkaStreams;

    public LatencyMetricExporter(final KafkaStreams kafkaStreams) {
        this.kafkaStreams = kafkaStreams;
    }

    @Override
    public void bindTo(final MeterRegistry registry) {
        //        final RemovalListener<String, AtomicDouble> cacheRemovalListener = notification -> {
        //            registry.remove(new Meter.Id());
        //        };
        //        final Cache<String, AtomicDouble> cache = CacheBuilder.newBuilder().build();
        //
        //        final ScheduledExecutorService mainExecutor = Executors.newSingleThreadScheduledExecutor();
        //        Shutdown.addShutdownHook("Latency Metrics Exporter", () -> {
        //            mainExecutor.shutdownNow();
        //            mainExecutor.awaitTermination(1, TimeUnit.SECONDS);
        //        });
        //        mainExecutor.scheduleAtFixedRate(() -> updateMetrics(registry, cache), 1, 1, TimeUnit.SECONDS);
    }

    //    private void updateMetrics(MeterRegistry registry, Cache<String, AtomicDouble> cache) {
    //        log.trace("Exporting metrics");
    //
    //        if (!kafkaStreams.state().equals(KafkaStreams.State.RUNNING)) {
    //            log.warn("It was not possible to export metrics because kafka stream is not running");
    //            return;
    //        }
    //
    //        final ReadOnlyKeyValueStore<String, MetricAverage> store = kafkaStreams.store(
    //                StoreQueryParameters.fromNameAndType("latency-metrics", QueryableStoreTypes.keyValueStore()));
    //
    //        try (KeyValueIterator<String, MetricAverage> records = store.all()) {
    //            while (records.hasNext()) {
    //                KeyValue<String, MetricAverage> record = records.next();
    //
    //                final String[] splittedKey = record.key.split("/");
    //                final String baseName = splittedKey[1];
    //                final String sourceServer = splittedKey[0];
    //
    //                final AtomicDouble meter = cache.getIfPresent(record.key + "_avg");
    //                if (meter == null) {
    //                    AtomicDouble newMeter = registry.gauge(
    //                            baseName + "_avg",
    //                            Tags.of("server", sourceServer),
    //                            new AtomicDouble(record.value.getAvg()));
    //                    cache.put(record.key + "_avg", newMeter);
    //                } else {
    //                    meter.set(record.value.getAvg());
    //                }
    //            }
    //        }
    //    }
    //
    //    public AtomicDouble getMetricWrapper(final KeyValue<String, MetricAverage> record, final String metricType,
    // final MeterRegistry registry, final Cache<String, AtomicDouble> cache) {
    //        final String[] splittedKey = record.key.split("/");
    //
    //        final String baseName = splittedKey[1];
    //        final String sourceServer = splittedKey[0];
    //
    //        AtomicDouble meter = cache.getIfPresent(record.key + "_" + metricType);
    //        if (meter == null) {
    //            return registry.gauge(
    //                    baseName + "_avg",
    //                    Tags.of("server", sourceServer),
    //                    new AtomicDouble(record.value.getAvg()));
    //        }
    //        return meter;
    //    }
}
