package io.littlehorse.canary.aggregator;

import static io.littlehorse.canary.aggregator.topology.MetricsTopology.METRICS_STORE;

import io.littlehorse.canary.Bootstrap;
import io.littlehorse.canary.aggregator.topology.MetricsTopology;
import io.littlehorse.canary.config.CanaryConfig;
import io.littlehorse.canary.config.KafkaStreamsConfig;
import io.littlehorse.canary.prometheus.PrometheusMetricStoreExporter;
import io.littlehorse.canary.util.Shutdown;
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

        final MetricsTopology metricsTopology =
                new MetricsTopology(config.getTopicName(), config.getAggregatorStoreRetentionMs());
        kafkaStreams = new KafkaStreams(metricsTopology.toTopology(), new StreamsConfig(kafkaStreamsConfigMap.toMap()));
        Shutdown.addShutdownHook("Aggregator Topology", kafkaStreams);

        kafkaStreams.start();
        log.trace("Initialized");
    }

    @Override
    public void bindTo(final MeterRegistry registry) {
        final KafkaStreamsMetrics kafkaStreamsMetrics = new KafkaStreamsMetrics(kafkaStreams);
        Shutdown.addShutdownHook("Aggregator Topology: Prometheus Exporter", kafkaStreamsMetrics);
        kafkaStreamsMetrics.bindTo(registry);

        final DiskSpaceMetrics diskSpaceMetrics = new DiskSpaceMetrics(new File(kafkaStreamsConfigMap.getStateDir()));
        diskSpaceMetrics.bindTo(registry);

        final PrometheusMetricStoreExporter prometheusMetricStoreExporter =
                new PrometheusMetricStoreExporter(kafkaStreams, METRICS_STORE);
        prometheusMetricStoreExporter.bindTo(registry);
    }
}
