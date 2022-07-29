package io.littlehorse.broker;

import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.Topology;
import io.littlehorse.broker.processor.LHTopology;
import io.littlehorse.broker.server.LHApi;
import io.littlehorse.common.LHConfig;

public class Broker {
    public static void main(String[] args) {
        LHConfig config = new LHConfig();

        Topology topology = LHTopology.initTopology(config);
        KafkaStreams scheduler = new KafkaStreams(topology, config.getStreamsConfig());

        LHApi api = new LHApi(config);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            scheduler.close();
            config.cleanup();
        }));

        scheduler.start();
        api.start();
    }
}
