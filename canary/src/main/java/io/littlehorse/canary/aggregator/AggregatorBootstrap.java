package io.littlehorse.canary.aggregator;

import static io.littlehorse.canary.aggregator.topology.MetricsTopology.METRICS_STORE;

import io.littlehorse.canary.aggregator.topology.MetricsTopology;
import io.littlehorse.canary.config.CanaryConfig;
import io.littlehorse.canary.config.KafkaStreamsConfig;
import io.littlehorse.canary.prometheus.PrometheusMetricStoreExporter;
import io.littlehorse.canary.util.ShutdownHook;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsConfig;

@Slf4j
public class AggregatorBootstrap implements MeterBinder {

    private final KafkaStreams kafkaStreams;
    private final KafkaStreamsConfig kafkaStreamsConfigMap;

    public AggregatorBootstrap(final CanaryConfig config) {
        kafkaStreamsConfigMap = config.toKafkaStreamsConfig();

        final MetricsTopology metricsTopology =
                new MetricsTopology(config.getTopicName(), config.getAggregatorStoreRetentionMs());
        kafkaStreams = new KafkaStreams(metricsTopology.toTopology(), new StreamsConfig(kafkaStreamsConfigMap.toMap()));
        ShutdownHook.add("Aggregator Topology", kafkaStreams);

        kafkaStreams.start();

        log.info("Aggregator Started");
    }

    @Override
    public void bindTo(final MeterRegistry registry) {
        final PrometheusMetricStoreExporter prometheusMetricStoreExporter =
                new PrometheusMetricStoreExporter(kafkaStreams, METRICS_STORE);
        prometheusMetricStoreExporter.bindTo(registry);
    }
}
