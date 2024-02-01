package io.littlehorse.canary.aggregator;

import com.google.protobuf.InvalidProtocolBufferException;
import io.littlehorse.canary.Bootstrap;
import io.littlehorse.canary.proto.Metric;
import io.littlehorse.canary.proto.StreamTopologyFailure;
import io.littlehorse.canary.util.Shutdown;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;

@Slf4j
public class AggregatorBootstrap implements Bootstrap {

    private static final Consumed<String, Bytes> SERDES = Consumed.with(Serdes.String(), Serdes.Bytes());

    public AggregatorBootstrap(final String metricsTopicName, final Map<String, Object> kafkaStreamsConfigMap) {
        final KafkaStreams kafkaStreams =
                new KafkaStreams(buildTopology(metricsTopicName), new StreamsConfig(kafkaStreamsConfigMap));
        Shutdown.addShutdownHook(kafkaStreams);
        kafkaStreams.start();

        log.trace("Initialized");
    }

    private static Metric toMetric(final Bytes value) {
        try {
            return Metric.parseFrom(value.get());
        } catch (InvalidProtocolBufferException e) {
            log.error("Error in stream topology {}", e.getMessage(), e);
            return Metric.newBuilder()
                    .setStreamTopologyFailure(StreamTopologyFailure.newBuilder().setMessage(e.getMessage()))
                    .build();
        }
    }

    private static Topology buildTopology(final String metricsTopicName) {
        final StreamsBuilder builder = new StreamsBuilder();

        final KStream<String, Metric> metricStream =
                builder.stream(metricsTopicName, SERDES).mapValues(AggregatorBootstrap::toMetric);

        metricStream.foreach((key, value) -> log.debug("Aggregating {} {}", value.getMetricCase(), key));

        return builder.build();
    }
}
