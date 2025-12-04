package io.littlehorse.server.streams;

import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.model.LHTimer;
import io.littlehorse.common.proto.Command;
import io.littlehorse.common.util.serde.ProtobufDeserializer;
import io.littlehorse.server.LHServer;
import io.littlehorse.server.streams.store.BoundedBytesSerde;
import io.littlehorse.server.streams.taskqueue.TaskQueueManager;
import io.littlehorse.server.streams.topology.core.CommandProcessorOutput;
import io.littlehorse.server.streams.topology.core.Forwardable;
import io.littlehorse.server.streams.topology.core.processors.CommandProcessor;
import io.littlehorse.server.streams.topology.core.processors.MetadataGlobalStoreProcessor;
import io.littlehorse.server.streams.topology.core.processors.ProcessorOutputRouter;
import io.littlehorse.server.streams.topology.core.processors.TimerCommandProcessor;
import io.littlehorse.server.streams.topology.timer.TimerProcessor;
import io.littlehorse.server.streams.util.AsyncWaiters;
import io.littlehorse.server.streams.util.MetadataCache;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.processor.api.ProcessorSupplier;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.StoreBuilder;
import org.apache.kafka.streams.state.Stores;

/**
 * The ServerTopologyV2 class is responsible for constructing storied architecture concerning the
 server's topology architecture of dependencies in operations and configurations on peripher function models injection server topology concepts.
 */
public class ServerTopologyV2 {

    public static final String COMMAND_PROCESSOR_NAME = ServerTopology.CORE_PROCESSOR;
    public static final String CORE_COMMAND_SOURCE_NAME = ServerTopology.CORE_SOURCE;
    public static final String TIMER_PROCESSOR_NAME = ServerTopology.TIMER_PROCESSOR;
    public static final String TIMER_PROCESSOR_ROUTER_PROCESSOR_NAME = "timer-processor-router-processor";
    public static final String TIMER_COMMAND_PROCESSOR_NAME = "timer-command-processor";
    public static final String OUTPUT_TOPIC_PROCESSOR_NAME = "output-topic-processor";
    public static final String ROUTER_PROCESSOR_NAME = "router-processor";
    public static final String REPARTITION_PASSTHROUGH_PROCESSOR = "passthrough-repartition-processor";
    public static final String TIMER_WITHOUT_FORWARD_PROCESSOR_NAME = "timer-without-forward-processor";
    public static final String CORE_STORE_NAME = ServerTopology.CORE_STORE;
    public static final String GLOBAL_METADATA_STORE_NAME = ServerTopology.GLOBAL_METADATA_STORE;
    public static final String GLOBAL_METADATA_SOURCE_NAME = ServerTopology.GLOBAL_METADATA_SOURCE;
    public static final String GLOBAL_METADATA_PROCESSOR_NAME = ServerTopology.GLOBAL_METADATA_PROCESSOR;

    private final ProcessorSupplier<String, Command, String, CommandProcessorOutput> commandProcessorSupplier;
    private final ProcessorSupplier<String, CommandProcessorOutput, String, Forwardable> routerProcessorSupplier;
    private final ProcessorSupplier<String, LHTimer, String, LHTimer> timerProcessorSupplier;
    private final ProcessorSupplier<String, LHTimer, String, LHSerializable<?>> timerProcessorRouterSupplier;
    private final ProcessorSupplier<String, Command, String, CommandProcessorOutput> timerCommandProcessorSupplier;
    private final ProcessorSupplier<String, Forwardable, String, Forwardable> passthroughRepartitionProcessor;
    private final ProcessorSupplier<String, LHTimer, String, LHTimer> timerWithoutForwardProcessorSupplier;
    private final String coreCommandTopic;
    private final String repartitionTopic;
    private final StoreBuilder<KeyValueStore<String, Bytes>> coreStoreBuilder;
    private final String metadataStoreChangelog;
    private final StoreBuilder<KeyValueStore<String, Bytes>> globalStoreBuilder;
    private final MetadataCache metadataCache;

    public ServerTopologyV2(
            LHServerConfig config,
            LHServer server,
            MetadataCache metadataCache,
            TaskQueueManager globalTaskQueueManager,
            AsyncWaiters asyncWaiters) {
        this.commandProcessorSupplier =
                () -> new CommandProcessor(config, server, metadataCache, globalTaskQueueManager, asyncWaiters);
        this.routerProcessorSupplier = ProcessorOutputRouter::createCommandProcessorRouter;
        this.timerProcessorSupplier = () -> new TimerProcessor(true, CORE_STORE_NAME);
        this.passthroughRepartitionProcessor = ProcessorOutputRouter::createPassthroughRepartitionRouter;
        this.timerProcessorRouterSupplier = ProcessorOutputRouter::createTimerProcessorRouter;
        this.timerCommandProcessorSupplier =
                () -> new TimerCommandProcessor(config, server, metadataCache, globalTaskQueueManager, asyncWaiters);
        this.timerWithoutForwardProcessorSupplier = () -> new TimerProcessor(false, CORE_STORE_NAME);
        this.coreCommandTopic = config.getCoreCmdTopicName();
        this.repartitionTopic = config.getRepartitionTopicName();
        this.coreStoreBuilder = Stores.keyValueStoreBuilder(
                Stores.persistentKeyValueStore(CORE_STORE_NAME),
                Serdes.String(),
                new BoundedBytesSerde(config.getProducerMaxRequestSize()));
        this.metadataStoreChangelog = LHServerConfig.getMetadataStoreChangelogTopic(config.getLHClusterId());
        this.globalStoreBuilder = Stores.keyValueStoreBuilder(
                        Stores.persistentKeyValueStore(GLOBAL_METADATA_STORE_NAME), Serdes.String(), Serdes.Bytes())
                .withLoggingDisabled();
        this.metadataCache = metadataCache;
    }



    public Topology build() {
        final Topology serverTopology = new Topology();
        serverTopology.addSource(
                CORE_COMMAND_SOURCE_NAME,
                Serdes.String().deserializer(),
                new ProtobufDeserializer<>(Command.parser()),
                coreCommandTopic,
                repartitionTopic);
        serverTopology.addProcessor(COMMAND_PROCESSOR_NAME, commandProcessorSupplier, CORE_COMMAND_SOURCE_NAME);
        serverTopology.addProcessor(ROUTER_PROCESSOR_NAME, routerProcessorSupplier, COMMAND_PROCESSOR_NAME);
        serverTopology.addProcessor(TIMER_PROCESSOR_NAME, timerProcessorSupplier, ROUTER_PROCESSOR_NAME);
        serverTopology.addProcessor(
                TIMER_PROCESSOR_ROUTER_PROCESSOR_NAME, timerProcessorRouterSupplier, TIMER_PROCESSOR_NAME);
        serverTopology.addProcessor(
                TIMER_COMMAND_PROCESSOR_NAME, timerCommandProcessorSupplier, TIMER_PROCESSOR_ROUTER_PROCESSOR_NAME);
        serverTopology.addProcessor(
                REPARTITION_PASSTHROUGH_PROCESSOR,
                passthroughRepartitionProcessor,
                TIMER_PROCESSOR_ROUTER_PROCESSOR_NAME);
        serverTopology.addProcessor(
                TIMER_WITHOUT_FORWARD_PROCESSOR_NAME,
                timerWithoutForwardProcessorSupplier,
                TIMER_COMMAND_PROCESSOR_NAME);

        serverTopology.addStateStore(
                coreStoreBuilder,
                COMMAND_PROCESSOR_NAME,
                TIMER_PROCESSOR_NAME,
                TIMER_COMMAND_PROCESSOR_NAME,
                TIMER_WITHOUT_FORWARD_PROCESSOR_NAME);

        serverTopology.addGlobalStore(
                globalStoreBuilder,
                GLOBAL_METADATA_SOURCE_NAME, // source created by Streams internally
                Serdes.String().deserializer(),
                Serdes.Bytes().deserializer(),
                metadataStoreChangelog, // input topic
                GLOBAL_METADATA_PROCESSOR_NAME,
                () -> new MetadataGlobalStoreProcessor(metadataCache));
        return serverTopology;
    }
}
