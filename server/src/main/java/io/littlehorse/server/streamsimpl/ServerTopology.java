package io.littlehorse.server.streamsimpl;

import io.littlehorse.common.LHConfig;
import io.littlehorse.common.model.LHTimer;
import io.littlehorse.common.model.command.CommandModel;
import io.littlehorse.common.util.serde.LHDeserializer;
import io.littlehorse.common.util.serde.LHSerde;
import io.littlehorse.server.KafkaStreamsServerImpl;
import io.littlehorse.server.streamsimpl.coreprocessors.CommandProcessor;
import io.littlehorse.server.streamsimpl.coreprocessors.CommandProcessorOutput;
import io.littlehorse.server.streamsimpl.coreprocessors.GlobalMetadataProcessor;
import io.littlehorse.server.streamsimpl.coreprocessors.RepartitionCommandProcessor;
import io.littlehorse.server.streamsimpl.coreprocessors.TimerProcessor;
import io.littlehorse.server.streamsimpl.coreprocessors.repartitioncommand.RepartitionCommand;
import io.littlehorse.server.streamsimpl.util.WfSpecCache;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.processor.TopicNameExtractor;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.StoreBuilder;
import org.apache.kafka.streams.state.Stores;

/*
 * There are multiple topologies here:
 * 1. Core topology.
 * 2. Timer topology.
 * 3. Metrics topology.
 *
 * It's important that all of them are separate toplogies because they need
 * separate transactional guarantees. For example, it's imperative that
 * all input messages into the Core Topology (i.e. the Core Command topic) are
 * NOT part of Kafka Transactions, because consumers block until the transaction
 * is committed. This would introduce significantly higher latency (100ms vs 20ms)
 * between task executions.
 *
 * The Core Topology has to be exactly-once-semantics (even though inputs aren't
 * transactional); the Timer Topology inputs into the Core Topology; therefore, if
 * the Timer and Core were combined then there would be transactional messages in
 * the Core Command topic (which means higher latency).
 *
 * The Timer Topology also doesn't need to be colocated with the Core Topology.
 * Later versions of LH may separate that out in order to segregate compute or
 * increase scalability.
 *
 * The Metrics topology is currently colocated with the Core Topology but does not
 * need to be. It can be separated out into a separate server (which would mean a
 * separte gRPC service) to improve horizonal scalability and separation of concerns.
 *
 * Eventually, the stuff in this file might get broken up into smaller files for
 * clarity.
 */
public class ServerTopology {

        public static final String TIMER_SOURCE = "timer-source";
        public static final String TIMER_PROCESSOR = "timer-processor";
        public static final String NEW_TIMER_SINK = "new-timer-sink";
        public static final String MATURED_TIMER_SINK = "matured-timer-sink";
        public static final String TIMER_STORE = "timer-store";

        public static final String CORE_SOURCE = "core-source";
        public static final String CORE_STORE = "core-store";
        public static final String CORE_PROCESSOR = "core-processor";
        public static final String CORE_REPARTITION_SINK = "core-repartition-sink";
        public static final String CORE_REPARTITION_SOURCE = "core-repartition-source";
        public static final String CORE_REPARTITION_STORE = "core-repartition-store";
        public static final String CORE_REPARTITION_PROCESSOR = "core-repartition-processor";

        public static final String GLOBAL_METADATA_SOURCE = "global-metadata-cl-source";
        public static final String GLOBAL_STORE = "global-metadata-store";
        public static final String GLOBAL_METADATA_PROCESSOR = "global-metadata-processor";

        public static final String METADATA_SOURCE = "metadata-source";

        public static final String METADATA_PROCESSOR = "metadata-processor";

        public static final String METADATA_STORE = "metadata-store";

        public static final String GLOBAL_METADATA_SINK = "global-metadata-sink";

        public static Topology initCoreTopology(LHConfig config, KafkaStreamsServerImpl server) {
                Topology topo = new Topology();
                WfSpecCache wfSpecCache = new WfSpecCache();
                Serializer<Object> sinkValueSerializer = (topic, output) -> {
                        CommandProcessorOutput cpo = (CommandProcessorOutput) output;
                        if (cpo.payload == null) {
                                return null;
                        }

                        return cpo.payload.toBytes(config);
                };

                TopicNameExtractor<String, Object> sinkTopicNameExtractor = (key, coreServerOutput,
                                ctx) -> ((CommandProcessorOutput) coreServerOutput).topic;

                topo.addSource(
                                CORE_SOURCE, // source name
                                Serdes.String().deserializer(), // key deserializer
                                new LHDeserializer<>(CommandModel.class, config), // value deserializer
                                config.getCoreCmdTopicName() // source topic
                );

                topo.addSource(
                                METADATA_SOURCE, // source name
                                Serdes.String().deserializer(), // key deserializer
                                new LHDeserializer<>(CommandModel.class, config), // value deserializer
                                config.getMetadataCmdTopicName() // source topic
                );

                topo.addProcessor(
                                METADATA_PROCESSOR,
                                () -> new CommandProcessor(config, server, wfSpecCache, METADATA_STORE, true),
                                METADATA_SOURCE);

                topo.addSink(
                                GLOBAL_METADATA_SINK,
                                sinkTopicNameExtractor, // topic extractor
                                Serdes.String().serializer(), // key serializer
                                sinkValueSerializer, // value serializer
                                METADATA_PROCESSOR // parent name
                );

                topo.addProcessor(
                                CORE_PROCESSOR,
                                () -> new CommandProcessor(config, server, wfSpecCache, CORE_STORE, false),
                                CORE_SOURCE);

                topo.addSink(
                                CORE_REPARTITION_SINK,
                                sinkTopicNameExtractor, // topic extractor
                                Serdes.String().serializer(), // key serializer
                                sinkValueSerializer, // value serializer
                                CORE_PROCESSOR // parent name
                );

                topo.addSource(
                                CORE_REPARTITION_SOURCE,
                                Serdes.String().deserializer(),
                                new LHDeserializer<>(RepartitionCommand.class, config),
                                config.getRepartitionTopicName());

                topo.addProcessor(
                                CORE_REPARTITION_PROCESSOR, () -> new RepartitionCommandProcessor(config),
                                CORE_REPARTITION_SOURCE);

                StoreBuilder<KeyValueStore<String, Bytes>> rePartitionedStoreBuilder = Stores.keyValueStoreBuilder(
                                Stores.persistentKeyValueStore(CORE_REPARTITION_STORE), Serdes.String(),
                                Serdes.Bytes());
                topo.addStateStore(rePartitionedStoreBuilder, CORE_REPARTITION_PROCESSOR);

                StoreBuilder<KeyValueStore<String, Bytes>> coreStoreBuilder = Stores.keyValueStoreBuilder(
                                Stores.persistentKeyValueStore(CORE_STORE), Serdes.String(), Serdes.Bytes());
                topo.addStateStore(coreStoreBuilder, CORE_PROCESSOR);

                StoreBuilder<KeyValueStore<String, Bytes>> metadataStoreBuilder = Stores.keyValueStoreBuilder(
                                Stores.persistentKeyValueStore(METADATA_STORE), Serdes.String(), Serdes.Bytes());
                topo.addStateStore(metadataStoreBuilder, METADATA_PROCESSOR);

                // There's a topic for global communication, which is used for two things:
                // 1. broadcasting global metadata to all instances
                // 2. (LATER) communicating about how many items are in each queue for
                // each partition.
                // This topic is config.getGlobalMetadatCLTopicName()
                // topo.addSource(globalMetaSource, Serdes.String().deserializer(), new
                // LHDeserializer<>(),
                // config))

                StoreBuilder<KeyValueStore<String, Bytes>> globalStoreBuilder = Stores.keyValueStoreBuilder(
                                Stores.persistentKeyValueStore(GLOBAL_STORE), Serdes.String(), Serdes.Bytes())
                                .withLoggingDisabled();

                topo.addGlobalStore(
                                globalStoreBuilder,
                                GLOBAL_METADATA_SOURCE,
                                Serdes.String().deserializer(),
                                Serdes.Bytes().deserializer(),
                                config.getGlobalMetadataCLTopicName(),
                                GLOBAL_METADATA_PROCESSOR,
                                () -> new GlobalMetadataProcessor(wfSpecCache));
                return topo;
        }

        public static Topology initTimerTopology(LHConfig config) {
                Topology topo = new Topology();
                Serde<LHTimer> timerSerde = new LHSerde<>(LHTimer.class, config);

                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                        timerSerde.close();
                }));

                topo.addSource(TIMER_SOURCE, Serdes.String().deserializer(), timerSerde.deserializer(),
                                config.getTimerTopic());

                topo.addProcessor(
                                TIMER_PROCESSOR,
                                () -> {
                                        return new TimerProcessor();
                                },
                                TIMER_SOURCE);

                topo.addSink(
                                MATURED_TIMER_SINK,
                                (key, lhTimer, ctx) -> {
                                        return ((LHTimer) lhTimer).topic;
                                },
                                Serdes.String().serializer(),
                                (topic, lhTimer) -> {
                                        return ((LHTimer) lhTimer).getPayload(config);
                                },
                                TIMER_PROCESSOR);

                // Add state store
                StoreBuilder<KeyValueStore<String, LHTimer>> timerStoreBuilder = Stores.keyValueStoreBuilder(
                                Stores.persistentKeyValueStore(TIMER_STORE), Serdes.String(), timerSerde);
                topo.addStateStore(timerStoreBuilder, TIMER_PROCESSOR);

                return topo;
        }
}
