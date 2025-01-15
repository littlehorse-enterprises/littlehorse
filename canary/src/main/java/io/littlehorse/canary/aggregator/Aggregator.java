package io.littlehorse.canary.aggregator;

import static io.littlehorse.canary.aggregator.topology.MetricsTopology.METRICS_STORE;

import io.littlehorse.canary.aggregator.prometheus.MetricStoreExporter;
import io.littlehorse.canary.aggregator.topology.MetricsTopology;
import io.littlehorse.canary.util.ShutdownHook;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import java.time.Duration;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsConfig;

@Slf4j
public class Aggregator implements MeterBinder {

    private final KafkaStreams kafkaStreams;

    public Aggregator(
            final Map<String, Object> kafkaStreamsConfig, final String inputTopic, final Duration storeRetention) {
        final MetricsTopology metricsTopology = new MetricsTopology(inputTopic, storeRetention);

        kafkaStreams = new KafkaStreams(metricsTopology.toTopology(), new StreamsConfig(kafkaStreamsConfig));
        ShutdownHook.add("Aggregator Topology", kafkaStreams);
        kafkaStreams.start();

        log.info("Aggregator Started");
    }

    @Override
    public void bindTo(final MeterRegistry registry) {
        final MetricStoreExporter prometheusMetricStoreExporter =
                new MetricStoreExporter(kafkaStreams, METRICS_STORE, Duration.ofSeconds(30));
        prometheusMetricStoreExporter.bindTo(registry);
    }
}
