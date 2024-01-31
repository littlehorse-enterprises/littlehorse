package io.littlehorse.canary.aggregator;

import com.google.protobuf.InvalidProtocolBufferException;
import io.littlehorse.canary.Bootstrap;
import io.littlehorse.canary.config.CanaryConfig;
import io.littlehorse.canary.proto.Metric;
import io.littlehorse.canary.proto.StreamTopologyFailure;
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
    private KafkaStreams kafkaStreams;

    private static Metric toMetric(Bytes value) {
        try {
            return Metric.parseFrom(value.get());
        } catch (InvalidProtocolBufferException e) {
            log.error("Error in stream topology {}", e.getMessage(), e);
            return Metric.newBuilder()
                    .setStreamTopologyFailure(StreamTopologyFailure.newBuilder().setMessage(e.getMessage()))
                    .build();
        }
    }

    private Topology buildTopology(CanaryConfig config) {
        StreamsBuilder builder = new StreamsBuilder();

        KStream<String, Metric> metricStream =
                builder.stream(config.getTopicName(), SERDES).mapValues(AggregatorBootstrap::toMetric);

        return builder.build();
    }

    @Override
    public void initialize(CanaryConfig config) {
        kafkaStreams = new KafkaStreams(
                buildTopology(config),
                new StreamsConfig(config.toKafkaStreamsConfig().toMap()));
        kafkaStreams.start();

        log.trace("Initialized");
    }

    @Override
    public void shutdown() {
        if (kafkaStreams != null) {
            kafkaStreams.close();
        }
        log.trace("Shutdown");
    }
}
