package io.littlehorse.server;

import io.littlehorse.common.LHConfig;
import io.littlehorse.common.util.LHKStreamsListener;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.TopologyDescription.Subtopology;

public class Server {

    public static void doMain(LHConfig config) {
        Topology serverTopo = ServerTopology.initMainTopology(config);

        for (Subtopology topo : serverTopo.describe().subtopologies()) {
            System.out.println(topo.toString());
            System.out.println();
        }

        KafkaStreams serverStreams = new KafkaStreams(
            serverTopo,
            config.getStreamsConfig("main")
        );
        ApiStreamsContext ctx = new ApiStreamsContext(config, serverStreams);
        LHKStreamsListener listener = new LHKStreamsListener();
        serverStreams.setStateListener(listener);
        serverStreams.setGlobalStateRestoreListener(listener);

        Topology timerTopo = ServerTopology.initTimerTopology(config);
        KafkaStreams timerStreams = new KafkaStreams(
            timerTopo,
            config.getStreamsConfig("timer")
        );

        serverStreams.start();
        timerStreams.start();

        LHApi app = new LHApi(config, ctx, listener);

        Runtime
            .getRuntime()
            .addShutdownHook(
                new Thread(() -> {
                    config.cleanup();
                    serverStreams.close();
                    timerStreams.close();
                })
            );

        app.start();
    }
}
