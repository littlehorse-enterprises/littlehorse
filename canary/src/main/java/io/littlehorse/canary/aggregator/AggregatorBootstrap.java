package io.littlehorse.canary.aggregator;

import static io.littlehorse.canary.aggregator.topology.CanaryTopology.METRICS_STORE;

import io.littlehorse.canary.Bootstrap;
import io.littlehorse.canary.aggregator.topology.CanaryTopology;
import io.littlehorse.canary.config.CanaryConfig;
import io.littlehorse.canary.config.KafkaStreamsConfig;
import io.littlehorse.canary.prometheus.PrometheusMetricStoreExporter;
import io.littlehorse.canary.util.ShutdownHook;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import io.micrometer.core.instrument.binder.kafka.KafkaStreamsMetrics;
import io.micrometer.core.instrument.binder.system.DiskSpaceMetrics;
import java.io.File;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsConfig;

@Slf4j
public class AggregatorBootstrap extends Bootstrap implements MeterBinder {

    private final KafkaStreams kafkaStreams;
    private final KafkaStreamsConfig kafkaStreamsConfigMap;

    public AggregatorBootstrap(final CanaryConfig config) {
        super(config);

        kafkaStreamsConfigMap = config.toKafkaStreamsConfig();

        final CanaryTopology canaryTopology =
                new CanaryTopology(config.getEventsTopicName(), config.getBeatsTopicName(), config.getAggregatorStoreRetentionMs());
        kafkaStreams = new KafkaStreams(canaryTopology.toTopology(), new StreamsConfig(kafkaStreamsConfigMap.toMap()));
        ShutdownHook.add("Aggregator Topology", kafkaStreams);

        kafkaStreams.start();

        log.info("Initialized");
    }

    @Override
    public void bindTo(final MeterRegistry registry) {
        final KafkaStreamsMetrics kafkaStreamsMetrics = new KafkaStreamsMetrics(kafkaStreams);
        ShutdownHook.add("Aggregator Topology: Prometheus Exporter", kafkaStreamsMetrics);
        kafkaStreamsMetrics.bindTo(registry);

        final DiskSpaceMetrics diskSpaceMetrics = new DiskSpaceMetrics(new File(kafkaStreamsConfigMap.getStateDir()));
        diskSpaceMetrics.bindTo(registry);

        final PrometheusMetricStoreExporter prometheusMetricStoreExporter = new PrometheusMetricStoreExporter(kafkaStreams, METRICS_STORE);
        prometheusMetricStoreExporter.bindTo(registry);
    }
}
