package io.littlehorse.server.streamsbackend;

import io.littlehorse.common.LHConfig;
import io.littlehorse.common.model.command.Command;
import io.littlehorse.common.model.wfrun.LHTimer;
import io.littlehorse.common.util.serde.LHDeserializer;
import io.littlehorse.common.util.serde.LHSerde;
import io.littlehorse.server.streamsbackend.coreprocessors.CommandProcessor;
import io.littlehorse.server.streamsbackend.coreprocessors.CommandProcessorOutput;
import io.littlehorse.server.streamsbackend.coreprocessors.GlobalMetadataProcessor;
import io.littlehorse.server.streamsbackend.coreprocessors.TimerProcessor;
import io.littlehorse.server.streamsbackend.taskqueue.GodzillaTaskQueueManager;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.StoreBuilder;
import org.apache.kafka.streams.state.Stores;

public class ServerTopology {

    public static String TIMER_SOURCE = "timer-source";
    public static String TIMER_PROCESSOR = "timer-processor";
    public static String NEW_TIMER_SINK = "new-timer-sink";
    public static String MATURED_TIMER_SINK = "matured-timer-sink";
    public static String TIMER_STORE = "timer-store";

    public static String CORE_SOURCE = "core-source";
    public static String CORE_STORE = "core-store";
    public static String CORE_PROCESSOR = "core-processor";
    public static String CORE_SINK = "core-sink";

    public static String GLOBAL_META_SOURCE = "global-metadata-cl-source";
    public static String GLOBAL_STORE = "global-metadata-store";
    public static String GLOBAL_META_PROCESSOR = "global-metadata-processor";

    public static Topology initCoreTopology(
        LHConfig config,
        GodzillaTaskQueueManager godzilla
    ) {
        Topology topo = new Topology();

        topo.addSource(
            CORE_SOURCE, // source name
            Serdes.String().deserializer(), // key deserializer
            new LHDeserializer<>(Command.class, config), // value deserializer
            config.getCoreCmdTopicName() // source topic
        );

        topo.addProcessor(
            CORE_PROCESSOR,
            () -> {
                return new CommandProcessor(config, godzilla);
            },
            CORE_SOURCE
        );

        topo.addSink(
            CORE_SINK,
            (key, coreServerOutput, ctx) -> {
                return ((CommandProcessorOutput) coreServerOutput).topic;
            }, // topic extractor
            Serdes.String().serializer(), // key serializer
            (topic, output) -> {
                CommandProcessorOutput cpo = (CommandProcessorOutput) output;
                if (cpo.payload == null) {
                    return null;
                }

                return cpo.payload.toBytes(config);
            }, // value serializer
            CORE_PROCESSOR // parent name
        );

        StoreBuilder<KeyValueStore<String, Bytes>> partitionedStoreBuilder = Stores.keyValueStoreBuilder(
            Stores.persistentKeyValueStore(CORE_STORE),
            Serdes.String(),
            Serdes.Bytes()
        );
        topo.addStateStore(partitionedStoreBuilder, CORE_PROCESSOR);

        // There's a topic for global communication, which is used for two things:
        // 1. broadcasting global metadata to all instances
        // 2. (LATER) communicating about how many items are in each queue for
        //    each partition.
        // This topic is config.getGlobalMetadatCLTopicName()
        // topo.addSource(globalMetaSource, Serdes.String().deserializer(), new LHDeserializer<>(), config))

        StoreBuilder<KeyValueStore<String, Bytes>> globalStoreBuilder = Stores
            .keyValueStoreBuilder(
                Stores.persistentKeyValueStore(GLOBAL_STORE),
                Serdes.String(),
                Serdes.Bytes()
            )
            .withLoggingDisabled();
        topo.addGlobalStore(
            globalStoreBuilder,
            GLOBAL_META_SOURCE,
            Serdes.String().deserializer(),
            Serdes.Bytes().deserializer(),
            config.getGlobalMetadataCLTopicName(),
            GLOBAL_META_PROCESSOR,
            () -> {
                return new GlobalMetadataProcessor();
            }
            // add lambda to return the processor
        );
        return topo;
    }

    public static Topology initTaggingTopology(LHConfig config) {
        // doesn't look like we need this until we implement the
        // REMOTE_HASH_UNCOUNTED storage type.
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
            TIMER_SOURCE,
            Serdes.String().deserializer(),
            timerSerde.deserializer(),
            config.getTimerTopic()
        );

        topo.addProcessor(
            TIMER_PROCESSOR,
            () -> {
                return new TimerProcessor();
            },
            TIMER_SOURCE
        );

        topo.addSink(
            MATURED_TIMER_SINK,
            (key, lhTimer, ctx) -> {
                return ((LHTimer) lhTimer).topic;
            },
            Serdes.String().serializer(),
            (topic, lhTimer) -> {
                return ((LHTimer) lhTimer).getPayload(config);
            },
            TIMER_PROCESSOR
        );

        // Add state store
        StoreBuilder<KeyValueStore<String, LHTimer>> timerStoreBuilder = Stores.keyValueStoreBuilder(
            Stores.persistentKeyValueStore(TIMER_STORE),
            Serdes.String(),
            timerSerde
        );
        topo.addStateStore(timerStoreBuilder, TIMER_PROCESSOR);

        return topo;
    }
}
