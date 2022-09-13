package io.littlehorse.server;

import io.littlehorse.common.LHConfig;
import io.littlehorse.common.util.LHKStreamsListener;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.Topology;

public class Server {

    public static void doMain(LHConfig config) {
        Topology serverTopo = ServerTopology.initMainTopology(config);
        KafkaStreams serverStreams = new KafkaStreams(
            serverTopo,
            config.getStreamsConfig("main")
        );
        ApiStreamsContext ctx = new ApiStreamsContext(config, serverStreams);
        LHKStreamsListener listener = new LHKStreamsListener();
        serverStreams.setStateListener(listener);
        serverStreams.setGlobalStateRestoreListener(listener);

        LHApi app = new LHApi(config, ctx, listener);

        Runtime
            .getRuntime()
            .addShutdownHook(
                new Thread(() -> {
                    config.cleanup();
                    serverStreams.close();
                })
            );

        Topology timerTopo = ServerTopology.initTimerTopology(config);
        KafkaStreams timerStreams = new KafkaStreams(
            timerTopo,
            config.getStreamsConfig("timer")
        );

        Runtime
            .getRuntime()
            .addShutdownHook(
                new Thread(() -> {
                    serverStreams.close();
                    timerStreams.close();
                })
            );

        app.start();
        serverStreams.start();
        timerStreams.start();
    }
}
