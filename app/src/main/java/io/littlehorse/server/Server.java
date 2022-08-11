package io.littlehorse.server;

import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.Topology;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.util.KStreamsStateListener;

public class Server {
    public static void doMain(LHConfig config) {
        Topology topo = ServerTopology.initTopology(config);
        KafkaStreams streams = new KafkaStreams(topo, config.getStreamsConfig());
        ApiStreamsContext ctx = new ApiStreamsContext(
            config,
            streams
        );
        KStreamsStateListener listener = new KStreamsStateListener();
        streams.setStateListener(listener);

        LHApi app = new LHApi(config, ctx, listener);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            config.cleanup();
            streams.close();
        }));
        app.start();
        streams.start();
    }
}
