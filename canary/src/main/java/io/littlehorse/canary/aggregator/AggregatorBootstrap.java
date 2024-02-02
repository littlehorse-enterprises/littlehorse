package io.littlehorse.canary.aggregator;

import io.littlehorse.canary.Bootstrap;
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
            Consumed.with(Serdes.String(), MetricSerdes.Metric()).withTimestampExtractor(new MetricTimeExtractor());

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

        buildTaskRunLatencyTopology(metricStream);

        return builder.build();
    }

    private static void buildTaskRunLatencyTopology(final KStream<String, Metric> metricStream) {
        final KStream<String, Metric> taskRunLatencyStream =
                metricStream.filter((key, value) -> value.hasTaskRunLatency());
        taskRunLatencyStream.peek((key, value) ->
                log.debug("Hello Mijail {} {} {}", value.getMetricCase(), key, value.getTaskRunLatency()));
    }
}
