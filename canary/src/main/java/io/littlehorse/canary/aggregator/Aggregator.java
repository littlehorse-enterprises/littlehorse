package io.littlehorse.canary.aggregator;

import static io.littlehorse.canary.aggregator.topology.MetricsTopology.METRICS_STORE;

import io.littlehorse.canary.aggregator.prometheus.MetricStoreExporter;
import io.littlehorse.canary.aggregator.topology.MetricsTopology;
import io.littlehorse.canary.infra.HealthStatusBinder;
import io.littlehorse.canary.infra.HealthStatusRegistry;
import io.littlehorse.canary.infra.ShutdownHook;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import java.time.Duration;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsConfig;

@Slf4j
public class Aggregator implements MeterBinder, HealthStatusBinder {

    private final KafkaStreams kafkaStreams;
    private final Duration exportFrequency;

    public Aggregator(
            final Map<String, Object> kafkaStreamsConfig,
            final String inputTopic,
            final Duration storeRetention,
            final Duration exportFrequency) {
        final MetricsTopology metricsTopology = new MetricsTopology(inputTopic, storeRetention);
        this.exportFrequency = exportFrequency;

        kafkaStreams = new KafkaStreams(metricsTopology.toTopology(), new StreamsConfig(kafkaStreamsConfig));
        ShutdownHook.add("Aggregator Topology", kafkaStreams);
    }

    public void start() {
        kafkaStreams.start();
        log.info("Aggregator Started");
    }

    @Override
    public void bindTo(final MeterRegistry registry) {
        final MetricStoreExporter prometheusMetricStoreExporter =
                new MetricStoreExporter(kafkaStreams, METRICS_STORE, exportFrequency);
        prometheusMetricStoreExporter.bindTo(registry);
    }

    @Override
    public void bindTo(final HealthStatusRegistry registry) {
        registry.addStatus("aggregator", () -> kafkaStreams.state().isRunningOrRebalancing());
    }
}
