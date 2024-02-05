package io.littlehorse.canary.aggregator;

import io.littlehorse.canary.aggregator.internal.MetricTimeExtractor;
import io.littlehorse.canary.aggregator.serdes.ProtobufSerdes;
import io.littlehorse.canary.aggregator.topology.TaskRunLatencyTopology;
import io.littlehorse.canary.prometheus.Measurable;
import io.littlehorse.canary.proto.Metric;
import io.littlehorse.canary.util.Shutdown;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.kafka.KafkaStreamsMetrics;
import io.micrometer.core.instrument.binder.system.DiskSpaceMetrics;
import java.io.File;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;

@Slf4j
public class AggregatorBootstrap implements Measurable {

    private static final Consumed<String, Metric> SERDES =
            Consumed.with(Serdes.String(), ProtobufSerdes.Metric()).withTimestampExtractor(new MetricTimeExtractor());
    private final KafkaStreams kafkaStreams;
    private final KafkaStreamsMetrics kafkaStreamsMetrics;
    private final Map<String, Object> kafkaStreamsConfigMap;

    public AggregatorBootstrap(final String metricsTopicName, final Map<String, Object> kafkaStreamsConfigMap) {
        this.kafkaStreamsConfigMap = kafkaStreamsConfigMap;
        kafkaStreams = new KafkaStreams(buildTopology(metricsTopicName), new StreamsConfig(this.kafkaStreamsConfigMap));
        Shutdown.addShutdownHook(kafkaStreams);
        kafkaStreams.start();

        kafkaStreamsMetrics = new KafkaStreamsMetrics(kafkaStreams);
        Shutdown.addShutdownHook(kafkaStreamsMetrics);

        log.trace("Initialized");
    }

    private static Topology buildTopology(final String metricsTopicName) {
        final StreamsBuilder builder = new StreamsBuilder();
        final KStream<String, Metric> metricStream = builder.stream(metricsTopicName, SERDES);
        new TaskRunLatencyTopology(metricStream);
        return builder.build();
    }

    public String getStateDir() {
        return kafkaStreamsConfigMap.get(StreamsConfig.STATE_DIR_CONFIG).toString();
    }

    @Override
    public void bindTo(final MeterRegistry registry) {
        kafkaStreamsMetrics.bindTo(registry);

        final DiskSpaceMetrics diskSpaceMetrics = new DiskSpaceMetrics(new File(getStateDir()));
        diskSpaceMetrics.bindTo(registry);
    }
}
