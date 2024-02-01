package io.littlehorse.canary.aggregator;

import com.google.protobuf.InvalidProtocolBufferException;
import io.littlehorse.canary.Bootstrap;
import io.littlehorse.canary.proto.Metric;
import io.littlehorse.canary.proto.Metric.MetricCase;
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
import org.apache.kafka.streams.kstream.*;

@Slf4j
public class AggregatorBootstrap implements Bootstrap {

    private static final Consumed<String, Bytes> SERDES = Consumed.with(Serdes.String(), Serdes.Bytes());
    public static final String PREFIX_BRANCH = "CANARY_";

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

        final Map<String, KStream<String, Metric>> branches = metricStream
                .split(Named.as(PREFIX_BRANCH))
                .branch((key, value) -> value.hasTaskRunLatency(), Branched.as(MetricCase.TASK_RUN_LATENCY.name()))
                .branch(
                        (key, value) -> value.hasDuplicatedTaskRun(),
                        Branched.as(MetricCase.DUPLICATED_TASK_RUN.name()))
                .branch(
                        (key, value) -> value.hasStreamTopologyFailure(),
                        Branched.as(MetricCase.STREAM_TOPOLOGY_FAILURE.name()))
                .noDefaultBranch();

        buildTaskRunLatencyTopology(branches.get(PREFIX_BRANCH + MetricCase.TASK_RUN_LATENCY.name()));

        return builder.build();
    }

    private static void buildTaskRunLatencyTopology(final KStream<String, Metric> taskRunLatencyStream) {
        taskRunLatencyStream.peek((key, value) -> log.debug("Here {}", value.getMetricCase()));
    }
}
