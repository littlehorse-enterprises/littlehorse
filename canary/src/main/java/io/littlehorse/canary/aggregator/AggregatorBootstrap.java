package io.littlehorse.canary.aggregator;

import io.littlehorse.canary.Bootstrap;
import io.littlehorse.canary.aggregator.internal.LatencyMetricExporter;
import io.littlehorse.canary.aggregator.internal.MetricTimeExtractor;
import io.littlehorse.canary.aggregator.serdes.ProtobufSerdes;
import io.littlehorse.canary.aggregator.topology.TaskRunLatencyTopology;
import io.littlehorse.canary.config.CanaryConfig;
import io.littlehorse.canary.config.KafkaStreamsConfig;
import io.littlehorse.canary.prometheus.Measurable;
import io.littlehorse.canary.proto.Metric;
import io.littlehorse.canary.proto.MetricAverage;
import io.littlehorse.canary.util.Shutdown;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.kafka.KafkaStreamsMetrics;
import io.micrometer.core.instrument.binder.system.DiskSpaceMetrics;
import java.io.File;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;

@Slf4j
public class AggregatorBootstrap extends Bootstrap implements Measurable {

    private static final Consumed<String, Metric> SERDES =
            Consumed.with(Serdes.String(), ProtobufSerdes.Metric()).withTimestampExtractor(new MetricTimeExtractor());
    private final KafkaStreams kafkaStreams;
    private final KafkaStreamsConfig kafkaStreamsConfigMap;

    public AggregatorBootstrap(final CanaryConfig config) {
        super(config);

        kafkaStreamsConfigMap = config.toKafkaStreamsConfig();
        kafkaStreams = new KafkaStreams(
                buildTopology(config.getTopicName()), new StreamsConfig(kafkaStreamsConfigMap.toMap()));
        Shutdown.addShutdownHook("Aggregator Topology", kafkaStreams);

        kafkaStreams.start();
        log.trace("Initialized");
    }

    private static Topology buildTopology(final String metricsTopicName) {
        final StreamsBuilder builder = new StreamsBuilder();
        final KStream<String, Metric> metricStream = builder.stream(metricsTopicName, SERDES);
        new TaskRunLatencyTopology(metricStream);
        return builder.build();
    }

    @Override
    public void bindTo(final MeterRegistry registry) {
        final KafkaStreamsMetrics kafkaStreamsMetrics = new KafkaStreamsMetrics(kafkaStreams);
        Shutdown.addShutdownHook("Aggregator Topology: Prometheus Exporter", kafkaStreamsMetrics);
        kafkaStreamsMetrics.bindTo(registry);

        final DiskSpaceMetrics diskSpaceMetrics = new DiskSpaceMetrics(new File(kafkaStreamsConfigMap.getStateDir()));
        diskSpaceMetrics.bindTo(registry);

        final ReadOnlyKeyValueStore<String, MetricAverage> store = null;
        //        kafkaStreams.store(
        //                StoreQueryParameters.fromNameAndType("latency-metrics", QueryableStoreTypes.keyValueStore()));
        final LatencyMetricExporter latencyMetricExporter = new LatencyMetricExporter(store);
        latencyMetricExporter.bindTo(registry);
    }
}
