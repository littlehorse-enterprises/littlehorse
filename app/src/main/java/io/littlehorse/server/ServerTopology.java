package io.littlehorse.server;

import io.littlehorse.common.LHConfig;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.model.command.Command;
import io.littlehorse.common.model.wfrun.LHTimer;
import io.littlehorse.common.util.serde.LHDeserializer;
import io.littlehorse.common.util.serde.LHSerde;
import io.littlehorse.server.oldprocessors.TimerProcessor;
import io.littlehorse.server.streamsbackend.coreserver.CoreServerProcessor;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.StoreBuilder;
import org.apache.kafka.streams.state.Stores;

public class ServerTopology {

    public static String timerSource = "timer-source";
    public static String timerProcessor = "timer-processor";
    public static String newTimerSink = "new-timer-sink";
    public static String maturedTimerSink = "matured-timer-sink";

    public static String coreSource = "core-source";
    public static String coreStore = "core-store";
    public static String coreProcessor = "core-processor";

    public static Topology initCoreTopology(LHConfig config) {
        Topology topo = new Topology();

        topo.addSource(
            coreSource, // source name
            Serdes.String().deserializer(), // key deserializer
            new LHDeserializer<>(Command.class, config), // value deserializer
            config.getCoreCmdTopicName() // source topic
        );

        topo.addProcessor(
            coreProcessor,
            () -> {
                return new CoreServerProcessor();
            },
            coreSource
        );

        return topo;
    }

    public static Topology initTaggingTopology(LHConfig config) {
        return null;
    }

    public static Topology initTimerTopology(LHConfig config) {
        Topology topo = new Topology();
        Serde<LHTimer> timerSerde = new LHSerde<>(LHTimer.class, config);

        Runtime
            .getRuntime()
            .addShutdownHook(
                new Thread(() -> {
                    timerSerde.close();
                })
            );

        topo.addSource(
            timerSource,
            Serdes.String().deserializer(),
            timerSerde.deserializer(),
            LHConstants.TIMER_TOPIC_NAME
        );

        topo.addProcessor(
            timerProcessor,
            () -> {
                return new TimerProcessor();
            },
            timerSource
        );

        topo.addSink(
            maturedTimerSink,
            (key, lhTimer, ctx) -> {
                return ((LHTimer) lhTimer).topic;
            },
            Serdes.String().serializer(),
            (topic, lhTimer) -> {
                return ((LHTimer) lhTimer).getPayload(config);
            },
            timerProcessor
        );

        // Add state store
        StoreBuilder<KeyValueStore<String, LHTimer>> timerStoreBuilder = Stores.keyValueStoreBuilder(
            Stores.persistentKeyValueStore(LHConstants.TIMER_STORE_NAME),
            Serdes.String(),
            timerSerde
        );
        topo.addStateStore(timerStoreBuilder, timerProcessor);

        return topo;
    }
}
