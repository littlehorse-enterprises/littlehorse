package io.littlehorse.canary.aggregator;

import io.littlehorse.canary.Bootstrap;
import io.littlehorse.canary.aggregator.internal.BeatTimeExtractor;
import io.littlehorse.canary.aggregator.internal.LatencyMetricExporter;
import io.littlehorse.canary.aggregator.serdes.ProtobufSerdes;
import io.littlehorse.canary.aggregator.topology.LatencyTopology;
import io.littlehorse.canary.config.CanaryConfig;
import io.littlehorse.canary.config.KafkaStreamsConfig;
import io.littlehorse.canary.proto.Beat;
import io.littlehorse.canary.proto.BeatKey;
import io.littlehorse.canary.util.Shutdown;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import io.micrometer.core.instrument.binder.kafka.KafkaStreamsMetrics;
import io.micrometer.core.instrument.binder.system.DiskSpaceMetrics;
import java.io.File;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.*;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;

@Slf4j
public class AggregatorBootstrap extends Bootstrap implements MeterBinder {

    private static final Consumed<BeatKey, Beat> SERDES = Consumed.with(ProtobufSerdes.BeatKey(), ProtobufSerdes.Beat())
            .withTimestampExtractor(new BeatTimeExtractor());
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
        final KStream<BeatKey, Beat> metricStream = builder.stream(metricsTopicName, SERDES);
        new LatencyTopology(metricStream);
        return builder.build();
    }

    @Override
    public void bindTo(final MeterRegistry registry) {
        final KafkaStreamsMetrics kafkaStreamsMetrics = new KafkaStreamsMetrics(kafkaStreams);
        Shutdown.addShutdownHook("Aggregator Topology: Prometheus Exporter", kafkaStreamsMetrics);
        kafkaStreamsMetrics.bindTo(registry);

        final DiskSpaceMetrics diskSpaceMetrics = new DiskSpaceMetrics(new File(kafkaStreamsConfigMap.getStateDir()));
        diskSpaceMetrics.bindTo(registry);

        final LatencyMetricExporter latencyMetricExporter = new LatencyMetricExporter(kafkaStreams);
        latencyMetricExporter.bindTo(registry);
    }
}
