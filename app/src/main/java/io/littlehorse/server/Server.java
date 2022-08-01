package io.littlehorse.server;

import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.Topology;
import io.littlehorse.common.LHConfig;

public class Server {
    public static void doMain(LHConfig config) {
        Topology topo = ServerTopology.initTopology(config);
        KafkaStreams streams = new KafkaStreams(topo, config.getStreamsConfig());
        ApiStreamsContext ctx = new ApiStreamsContext(config, streams);

        LHApi app = new LHApi(config, ctx);

        streams.start();
        app.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            config.cleanup();
            streams.close();
        }));
    }
}
