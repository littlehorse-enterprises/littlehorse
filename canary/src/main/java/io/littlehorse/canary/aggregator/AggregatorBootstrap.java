package io.littlehorse.canary.aggregator;

import io.littlehorse.canary.Bootstrap;
import io.littlehorse.canary.aggregator.internal.MetricTimeExtractor;
import io.littlehorse.canary.aggregator.serdes.ProtobufSerdes;
import io.littlehorse.canary.aggregator.topology.TaskRunLatencyTopology;
import io.littlehorse.canary.proto.Metric;
import io.littlehorse.canary.util.Shutdown;
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
public class AggregatorBootstrap implements Bootstrap {

    private static final Consumed<String, Metric> SERDES =
            Consumed.with(Serdes.String(), ProtobufSerdes.Metric()).withTimestampExtractor(new MetricTimeExtractor());

    public AggregatorBootstrap(final String metricsTopicName, final Map<String, Object> kafkaStreamsConfigMap) {
        final KafkaStreams kafkaStreams =
                new KafkaStreams(buildTopology(metricsTopicName), new StreamsConfig(kafkaStreamsConfigMap));
        Shutdown.addShutdownHook(kafkaStreams);
        kafkaStreams.start();

        log.trace("Initialized");
    }

    private static Topology buildTopology(final String metricsTopicName) {
        final StreamsBuilder builder = new StreamsBuilder();
        final KStream<String, Metric> metricStream = builder.stream(metricsTopicName, SERDES);
        new TaskRunLatencyTopology(metricStream);
        return builder.build();
    }
}
