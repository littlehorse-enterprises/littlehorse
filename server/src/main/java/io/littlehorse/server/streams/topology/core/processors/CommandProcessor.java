package io.littlehorse.server.streams.topology.core.processors;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.model.LHTimer;
import io.littlehorse.common.model.ScheduledTaskModel;
import io.littlehorse.common.model.corecommand.CommandModel;
import io.littlehorse.common.model.getable.global.acl.TenantModel;
import io.littlehorse.common.model.getable.objectId.PrincipalIdModel;
import io.littlehorse.common.model.metadatacommand.subcommand.AggregateWindowMetricsModel;
import io.littlehorse.common.proto.Command;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.sdk.common.proto.Tenant;
import io.littlehorse.server.LHServer;
import io.littlehorse.server.streams.ServerTopology;
import io.littlehorse.server.streams.store.LHIterKeyValue;
import io.littlehorse.server.streams.store.LHKeyValueIterator;
import io.littlehorse.server.streams.store.StoredGetable;
import io.littlehorse.server.streams.storeinternals.TaskQueueHintModel;
import io.littlehorse.server.streams.stores.ClusterScopedStore;
import io.littlehorse.server.streams.stores.TenantScopedStore;
import io.littlehorse.server.streams.taskqueue.TaskQueueManager;
import io.littlehorse.server.streams.topology.core.BackgroundContext;
import io.littlehorse.server.streams.topology.core.CommandProcessorOutput;
import io.littlehorse.server.streams.topology.core.CoreCommandException;
import io.littlehorse.server.streams.topology.core.CoreProcessorContext;
import io.littlehorse.server.streams.topology.core.LHProcessingExceptionHandler;
import io.littlehorse.server.streams.topology.core.PartitionMetricWindowModel;
import io.littlehorse.server.streams.util.AsyncWaiters;
import io.littlehorse.server.streams.util.HeadersUtil;
import io.littlehorse.server.streams.util.MetadataCache;
import java.time.Duration;
import java.util.Date;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.processor.PunctuationType;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.state.KeyValueStore;

@Slf4j
public class CommandProcessor implements Processor<String, Command, String, CommandProcessorOutput> {

    protected ProcessorContext<String, CommandProcessorOutput> ctx;
    private final LHServerConfig config;
    private final LHServer server;
    private final MetadataCache metadataCache;
    private final TaskQueueManager globalTaskQueueManager;

    protected KeyValueStore<String, Bytes> nativeStore;
    protected KeyValueStore<String, Bytes> globalStore;
    private boolean partitionIsClaimed;
    private final AsyncWaiters asyncWaiters;

    private final LHProcessingExceptionHandler exceptionHandler;

    public CommandProcessor(
            LHServerConfig config,
            LHServer server,
            MetadataCache metadataCache,
            TaskQueueManager globalTaskQueueManager,
            AsyncWaiters asyncWaiters) {
        this.config = config;
        this.server = server;
        this.metadataCache = metadataCache;
        this.globalTaskQueueManager = globalTaskQueueManager;
        this.exceptionHandler = new LHProcessingExceptionHandler(server, asyncWaiters);
        this.asyncWaiters = asyncWaiters;
    }

    @Override
    public void init(final ProcessorContext<String, CommandProcessorOutput> ctx) {
        log.info("Starting the init() process on partition {}", ctx.taskId().partition());
        this.ctx = ctx;
        this.nativeStore = ctx.getStateStore(ServerTopology.CORE_STORE);
        this.globalStore = ctx.getStateStore(ServerTopology.GLOBAL_METADATA_STORE);
        onPartitionClaimed();
        ctx.schedule(Duration.ofSeconds(30), PunctuationType.WALL_CLOCK_TIME, this::forwardWindowPartitionMetrics);
        log.info("Completed the init() process on partition {}", ctx.taskId().partition());
    }

    @Override
    public void process(final Record<String, Command> commandRecord) {
        exceptionHandler.tryRun(() -> processHelper(commandRecord));
    }

    private void processHelper(final Record<String, Command> commandRecord) {
        CoreProcessorContext executionContext = buildExecutionContext(commandRecord);
        CommandModel command = executionContext.currentCommand();
        log.trace(
                "{} Processing command of type {} with commandId {} with partition key {}",
                config.getLHInstanceName(),
                command.type,
                command.getCommandId(),
                command.getPartitionKey());
        try {
            Message response = command.process(executionContext, config);
            executionContext.endExecution();

            if (command.hasResponse()) {
                CompletableFuture<Message> completable = asyncWaiters.getOrRegisterFuture(
                        command.getCommandId().get(), Message.class, new CompletableFuture<>());
                completable.complete(response);
            }
        } catch (KafkaException ke) {
            throw ke;
        } catch (Exception exn) {
            throw new CoreCommandException(exn, command);
        }
    }

    private CoreProcessorContext buildExecutionContext(Record<String, Command> commandRecord) {
        Headers metadataHeaders = commandRecord.headers();
        Command commandToProcess = commandRecord.value();
        return new CoreProcessorContext(
                commandToProcess, metadataHeaders, config, ctx, globalTaskQueueManager, metadataCache, server);
    }

    public void onPartitionClaimed() {
        if (partitionIsClaimed) {
            throw new RuntimeException("Re-claiming partition! Yikes!");
        }
        partitionIsClaimed = true;
        server.drainPartitionTaskQueue(ctx.taskId());
        ClusterScopedStore clusterStore = ClusterScopedStore.newInstance(this.globalStore, new BackgroundContext());
        try (LHKeyValueIterator<?> storedTenants = clusterStore.range(
                GetableClassEnum.TENANT.getNumber() + "/",
                GetableClassEnum.TENANT.getNumber() + "/~",
                StoredGetable.class)) {
            storedTenants.forEachRemaining(getable -> {
                TenantModel storedTenant = ((StoredGetable<Tenant, TenantModel>) getable.getValue()).getStoredObject();
                rehydrateTenant(storedTenant);
            });
        }
    }

    private void rehydrateTenant(TenantModel tenant) {
        TenantScopedStore coreDefaultStore =
                TenantScopedStore.newInstance(this.nativeStore, tenant.getId(), new BackgroundContext());

        TaskQueueHintModel hint =
                coreDefaultStore.get(TaskQueueHintModel.TASK_QUEUE_HINT_KEY, TaskQueueHintModel.class);

        if (hint == null) {
            log.warn("Could not find task queue hint, may need to iterate over many tombstones");
        }
        String startKey = hint == null ? "" : hint.getKeyToResumeFrom();
        String endKey = "~";
        try (LHKeyValueIterator<ScheduledTaskModel> iter =
                coreDefaultStore.range(startKey, endKey, ScheduledTaskModel.class)) {
            while (iter.hasNext()) {
                LHIterKeyValue<ScheduledTaskModel> next = iter.next();
                ScheduledTaskModel scheduledTask = next.getValue();
                log.debug("Rehydration: scheduling task: {}", scheduledTask.getStoreKey());
                // This will break task rehydration for tenant specific test. this will be addressed in Issue #554
                server.onTaskScheduled(
                        ctx.taskId(), scheduledTask.getTaskDefId(), scheduledTask.getTaskRunId(), tenant.getId());
            }
        }
    }

    @Override
    public void close() {
        this.partitionIsClaimed = false;
        server.drainPartitionTaskQueue(ctx.taskId());
    }

    private void forwardWindowPartitionMetrics(long timestamp) {
        ClusterScopedStore clusterScopedStore =
                ClusterScopedStore.newInstance(ctx.getStateStore(ServerTopology.CORE_STORE), new BackgroundContext());

        long startTime = timestamp - (2 * 60 * 60 * 1000L);
        String startKey = "metrics/partition/" + startTime;
        String endKey = "metrics/partition/~";

        try (LHKeyValueIterator<PartitionMetricWindowModel> iter =
                clusterScopedStore.range(startKey, endKey, PartitionMetricWindowModel.class)) {
            while (iter.hasNext()) {
                LHIterKeyValue<PartitionMetricWindowModel> next = iter.next();
                PartitionMetricWindowModel metricWindow = next.getValue();
                if (metricWindow != null) {
                    AggregateWindowMetricsModel aggregateMetrics =
                            new AggregateWindowMetricsModel(metricWindow.getTenantId(), metricWindow);
                    forwardSubcommand(aggregateMetrics);
                    clusterScopedStore.delete(metricWindow);
                }
            }
        }
    }

    private void forwardSubcommand(AggregateWindowMetricsModel subCommand) {
        CommandModel command = new CommandModel(subCommand, new Date());
        LHTimer timer = new LHTimer(command);
        timer.maturationTime = new Date();
        timer.setRepartition(true);
        timer.topic = this.config.getCoreCmdTopicName();
        CommandProcessorOutput cpo = new CommandProcessorOutput();
        cpo.partitionKey = subCommand.getPartitionKey();
        cpo.topic = this.config.getCoreCmdTopicName();
        cpo.payload = timer;
        Record<String, CommandProcessorOutput> out = new Record<>(
                cpo.partitionKey,
                cpo,
                System.currentTimeMillis(),
                HeadersUtil.metadataHeadersFor(
                        subCommand.getTenantId(), new PrincipalIdModel(LHConstants.ANONYMOUS_PRINCIPAL)));
        this.ctx.forward(out);
    }
}
