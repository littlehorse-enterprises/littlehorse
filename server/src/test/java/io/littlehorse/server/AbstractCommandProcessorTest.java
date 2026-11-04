package io.littlehorse.server;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.model.CoreGetable;
import io.littlehorse.common.model.LHTimer;
import io.littlehorse.common.model.MetadataGetable;
import io.littlehorse.common.model.corecommand.CommandModel;
import io.littlehorse.common.model.getable.CoreObjectId;
import io.littlehorse.common.model.getable.global.acl.PrincipalModel;
import io.littlehorse.common.model.getable.global.acl.TenantModel;
import io.littlehorse.common.model.getable.objectId.PrincipalIdModel;
import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import io.littlehorse.common.proto.Command;
import io.littlehorse.common.proto.MetadataCommand;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.Principal;
import io.littlehorse.sdk.common.proto.ServerACLs;
import io.littlehorse.server.monitoring.metrics.CommandProcessorMetrics;
import io.littlehorse.server.streams.ServerTopology;
import io.littlehorse.server.streams.storeinternals.MetadataManager;
import io.littlehorse.server.streams.stores.ClusterScopedStore;
import io.littlehorse.server.streams.stores.TenantScopedStore;
import io.littlehorse.server.streams.taskqueue.TaskQueueManager;
import io.littlehorse.server.streams.topology.core.BackgroundContext;
import io.littlehorse.server.streams.topology.core.CommandProcessorOutput;
import io.littlehorse.server.streams.topology.core.CoreStoreProvider;
import io.littlehorse.server.streams.topology.core.RequestExecutionContext;
import io.littlehorse.server.streams.topology.core.processors.CommandProcessor;
import io.littlehorse.server.streams.topology.core.processors.MetadataProcessor;
import io.littlehorse.server.streams.util.AsyncWaiters;
import io.littlehorse.server.streams.util.HeadersUtil;
import io.littlehorse.server.streams.util.MetadataCache;
import java.io.IOException;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Consumer;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.processor.api.MockProcessorContext;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.Stores;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

/** Runs real commands and persistence against Kafka Streams' in-memory stores, without a broker. */
public abstract class AbstractCommandProcessorTest {
    protected record ForwardedCommand(Command command, String partitionKey) {}

    protected final MockProcessorContext<String, CommandProcessorOutput> streamsContext = new MockProcessorContext<>();
    protected final LHServerConfig config = mock();
    protected final LHServer server = mock();
    protected final TaskQueueManager taskQueueManager = mock();
    protected TenantIdModel tenantId = new TenantIdModel(LHConstants.DEFAULT_TENANT);
    protected PrincipalIdModel principalId = new PrincipalIdModel(LHConstants.ANONYMOUS_PRINCIPAL);
    protected Instant commandTime = Instant.ofEpochMilli(1_700_000_000_000L);
    private List<ForwardedCommand> forwardedCommands = List.of();

    private final MetadataCache metadataCache = new MetadataCache();
    private final AsyncWaiters responses = new AsyncWaiters();
    private final CoreStoreProvider storeProvider = mock();
    private KeyValueStore<String, Bytes> coreStore;
    private KeyValueStore<String, Bytes> globalStore;
    private CommandProcessor processor;
    private KeyValueStore<String, Bytes> metadataStore;
    private MetadataProcessor metadataProcessor;

    @BeforeEach
    final void setUpCommandProcessor() {
        coreStore = createStore(ServerTopology.CORE_STORE);
        globalStore = createStore(ServerTopology.GLOBAL_METADATA_STORE);
        when(config.getProducerMaxRequestSize()).thenReturn(Integer.MAX_VALUE);
        when(config.getActiveThreadRunsPerWfRun()).thenReturn(65);
        when(config.getCoreCmdTopicName()).thenReturn("test-core-command");
        when(config.getMetadataCmdTopicName()).thenReturn("test-metadata-command");
        when(config.getTimerTopic()).thenReturn("test-timer");
        when(config.getMaxBulkJobCommandsPerTick()).thenReturn(100L);
        when(storeProvider.nativeCoreStore()).thenReturn(coreStore);
        when(storeProvider.nativeCoreStore(anyInt())).thenReturn(coreStore);
        when(storeProvider.getNativeGlobalStore()).thenReturn(globalStore);
        when(server.getCoreStoreProvider()).thenReturn(storeProvider);

        MetadataManager metadata = metadataWriter();
        metadata.put(new TenantModel(tenantId));
        metadata.put(PrincipalModel.fromProto(
                Principal.newBuilder()
                        .setId(principalId.toProto())
                        .setGlobalAcls(ServerACLs.newBuilder().addAcls(LHConstants.ADMIN_ACL))
                        .build(),
                PrincipalModel.class,
                new BackgroundContext()));

        processor = new CommandProcessor(
                config, server, metadataCache, taskQueueManager, responses, mock(CommandProcessorMetrics.class));
        processor.init(streamsContext);
    }

    @AfterEach
    final void tearDownCommandProcessor() {
        try {
            // Stop the background worker before closing stores that it reads.
            if (processor != null) processor.close();
        } finally {
            if (coreStore != null) coreStore.close();
            if (globalStore != null) globalStore.close();
            if (metadataStore != null) metadataStore.close();
        }
    }

    protected final <R extends Message> R execute(
            String partitionKey, Consumer<Command.Builder> payload, Class<R> responseType) {
        Command command =
                command(payload).setCommandId(UUID.randomUUID().toString()).build();
        return executeAndAwait(command.getCommandId(), responseType, () -> process(partitionKey, command));
    }

    /** Delivers the original forwarded command without waiting for a broker or timer. */
    protected final void executeForwardedCommand(ForwardedCommand forwarded) {
        process(forwarded.partitionKey(), forwarded.command());
    }

    /** Returns an immutable snapshot of the commands forwarded by the last core command. */
    protected final List<ForwardedCommand> forwardedCommands() {
        return List.copyOf(forwardedCommands);
    }

    /** Validates metadata through the real processor, then synchronously mirrors its changelog state. */
    protected final <R extends Message> R executeMetadata(
            Consumer<MetadataCommand.Builder> payload, Class<R> responseType) {
        if (metadataStore == null) {
            metadataStore = createStore(ServerTopology.METADATA_STORE);
            mirrorStore(globalStore, metadataStore);
            metadataProcessor = new MetadataProcessor(
                    config, server, metadataCache, responses, mock(CommandProcessorMetrics.class));
            metadataProcessor.init(streamsContext);
        } else {
            mirrorStore(globalStore, metadataStore);
        }
        MetadataCommand.Builder builder = MetadataCommand.newBuilder();
        payload.accept(builder);
        MetadataCommand command = builder.setCommandId(UUID.randomUUID().toString())
                .setTime(LHUtil.fromDate(Date.from(commandTime)))
                .build();
        R response = executeAndAwait(
                command.getCommandId(),
                responseType,
                () -> metadataProcessor.process(new Record<>(
                        command.getCommandId(),
                        command,
                        commandTime.toEpochMilli(),
                        HeadersUtil.metadataHeadersFor(tenantId, principalId))));
        // No Kafka broker is involved: make the validated metadata available to core commands.
        mirrorStore(metadataStore, globalStore);
        metadataCache.clear();
        return response;
    }

    private <R extends Message> R executeAndAwait(String commandId, Class<R> responseType, Runnable action) {
        CompletableFuture<Message> response =
                responses.getOrRegisterFuture(commandId, responseType, new CompletableFuture<>());
        try {
            action.run();
            assertThat(response.isDone())
                    .as("Command %s must produce a response", commandId)
                    .isTrue();
            return responseType.cast(response.join());
        } catch (CompletionException exception) {
            if (exception.getCause() instanceof RuntimeException cause) throw cause;
            if (exception.getCause() instanceof Error cause) throw cause;
            throw new AssertionError("Command failed", exception.getCause());
        } finally {
            responses.removeCommand(commandId);
        }
    }

    /** Processes an internal command without a response; assert its persisted effects separately. */
    protected final void processWithoutResponse(String partitionKey, Consumer<Command.Builder> payload) {
        process(partitionKey, command(payload).clearCommandId().build());
    }

    /** Each read uses a new context so it cannot see another command's pending changes. */
    protected final <U extends Message, T extends CoreGetable<U>> T readGetable(CoreObjectId<?, U, T> id) {
        return readContext().getableManager().get(id);
    }

    protected final RequestExecutionContext readContext() {
        return new RequestExecutionContext(principalId, tenantId, storeProvider, new MetadataCache(), config, false);
    }

    protected final <U extends Message, T extends MetadataGetable<U>> void seedMetadata(T value) {
        metadataWriter().put(value);
        metadataCache.clear();
    }

    private MetadataManager metadataWriter() {
        BackgroundContext context = new BackgroundContext();
        return new MetadataManager(
                ClusterScopedStore.newInstance(globalStore, context),
                TenantScopedStore.newInstance(globalStore, tenantId, context),
                metadataCache);
    }

    private Command.Builder command(Consumer<Command.Builder> payload) {
        Command.Builder command = Command.newBuilder();
        payload.accept(command);
        return command.setTime(LHUtil.fromDate(Date.from(commandTime)));
    }

    private void process(String partitionKey, Command command) {
        int firstForward = streamsContext.forwarded().size();
        forwardedCommands = List.of();
        processor.process(new Record<>(
                partitionKey,
                command,
                LHUtil.fromProtoTs(command.getTime()).getTime(),
                HeadersUtil.metadataHeadersFor(tenantId, principalId)));
        forwardedCommands = forwardedCommandsSince(firstForward);
    }

    private List<ForwardedCommand> forwardedCommandsSince(int firstForward) {
        return streamsContext
                .forwarded()
                .subList(firstForward, streamsContext.forwarded().size())
                .stream()
                .map(forward -> forward.record().value())
                .filter(output -> output.getPayload() instanceof LHTimer || output.getPayload() instanceof CommandModel)
                .map(output -> {
                    if (output.getPayload() instanceof LHTimer timer) {
                        try {
                            return new ForwardedCommand(
                                    Command.parseFrom(timer.getPayload()), output.getPartitionKey());
                        } catch (IOException exception) {
                            throw new AssertionError("Invalid forwarded timer command", exception);
                        }
                    }
                    Command command =
                            ((CommandModel) output.getPayload()).toProto().build();
                    return new ForwardedCommand(command, output.getPartitionKey());
                })
                .toList();
    }

    private KeyValueStore<String, Bytes> createStore(String name) {
        KeyValueStore<String, Bytes> store = Stores.keyValueStoreBuilder(
                        Stores.inMemoryKeyValueStore(name), Serdes.String(), Serdes.Bytes())
                .withLoggingDisabled()
                .build();
        store.init(streamsContext.getStateStoreContext(), store);
        return store;
    }

    private void mirrorStore(KeyValueStore<String, Bytes> source, KeyValueStore<String, Bytes> destination) {
        Map<String, Bytes> snapshot = new HashMap<>();
        try (KeyValueIterator<String, Bytes> entries = source.all()) {
            entries.forEachRemaining(entry -> snapshot.put(entry.key, entry.value));
        }
        try (KeyValueIterator<String, Bytes> entries = destination.all()) {
            entries.forEachRemaining(entry -> {
                if (!snapshot.containsKey(entry.key)) destination.delete(entry.key);
            });
        }
        snapshot.forEach(destination::put);
    }
}
