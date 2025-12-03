package io.littlehorse.server.streams;

import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.model.LHTimer;
import io.littlehorse.common.proto.Command;
import io.littlehorse.common.util.serde.ProtobufDeserializer;
import io.littlehorse.server.LHServer;
import io.littlehorse.server.streams.taskqueue.TaskQueueManager;
import io.littlehorse.server.streams.topology.core.CommandProcessorOutput;
import io.littlehorse.server.streams.topology.core.Forwardable;
import io.littlehorse.server.streams.topology.core.processors.CommandProcessor;
import io.littlehorse.server.streams.topology.core.processors.ProcessorOutputRouter;
import io.littlehorse.server.streams.topology.timer.TimerProcessor;
import io.littlehorse.server.streams.util.AsyncWaiters;
import io.littlehorse.server.streams.util.MetadataCache;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.processor.api.ProcessorSupplier;

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
public class ServerTopologyV2 {

    public static final String COMMAND_PROCESSOR_NAME = ServerTopology.CORE_PROCESSOR;
    public static final String CORE_COMMAND_SOURCE_NAME = ServerTopology.CORE_SOURCE;
    public static final String REPARTITION_SOURCE_NAME = ServerTopology.CORE_REPARTITION_SOURCE;
    public static final String TIMER_PROCESSOR_NAME = ServerTopology.TIMER_PROCESSOR;
    public static final String OUTPUT_TOPIC_PROCESSOR_NAME = "output-topic-processor";
    public static final String ROUTER_PROCESSOR_NAME = "router-processor";
    public static final String[] COMMAND_SOURCE_NAMES =
            new String[] {CORE_COMMAND_SOURCE_NAME, REPARTITION_SOURCE_NAME};

    private final ProcessorSupplier<String, Command, String, CommandProcessorOutput> commandProcessorSupplier;
    private final ProcessorSupplier<String, CommandProcessorOutput, String, Forwardable> routerProcessorSupplier;
    private final ProcessorSupplier<String, LHTimer, String, LHTimer> timerProcessorSupplier;

    public ServerTopologyV2(
            LHServerConfig config,
            LHServer server,
            MetadataCache metadataCache,
            TaskQueueManager globalTaskQueueManager,
            AsyncWaiters asyncWaiters) {
        this.commandProcessorSupplier =
                () -> new CommandProcessor(config, server, metadataCache, globalTaskQueueManager, asyncWaiters);
        this.routerProcessorSupplier = ProcessorOutputRouter::new;
        this.timerProcessorSupplier = TimerProcessor::new;
    }

    public Topology build() {
        final Topology serverTopology = new Topology();
        serverTopology.addSource(
                CORE_COMMAND_SOURCE_NAME, Serdes.String().deserializer(), new ProtobufDeserializer<>(Command.parser()));
        serverTopology.addSource(
                REPARTITION_SOURCE_NAME, Serdes.String().deserializer(), new ProtobufDeserializer<>(Command.parser()));
        serverTopology.addProcessor(COMMAND_PROCESSOR_NAME, commandProcessorSupplier, COMMAND_SOURCE_NAMES);
        serverTopology.addProcessor(ROUTER_PROCESSOR_NAME, routerProcessorSupplier, COMMAND_PROCESSOR_NAME);
        serverTopology.addProcessor(TIMER_PROCESSOR_NAME, timerProcessorSupplier, ROUTER_PROCESSOR_NAME);
        return serverTopology;
    }
}
