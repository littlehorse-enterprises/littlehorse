package io.littlehorse.server.streams.topology.core.processors;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.model.PartitionCountedTagModel;
import io.littlehorse.common.model.PartitionMetricWindowModel;
import io.littlehorse.common.model.ScheduledTaskModel;
import io.littlehorse.common.model.corecommand.CommandModel;
import io.littlehorse.common.model.corecommand.subcommand.DeleteMetricWindowModel;
import io.littlehorse.common.model.corecommand.subcommand.UpdateCountedTagModel;
import io.littlehorse.common.model.getable.global.acl.TenantModel;
import io.littlehorse.common.model.getable.objectId.MetricWindowIdModel;
import io.littlehorse.common.model.getable.objectId.PrincipalIdModel;
import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import io.littlehorse.common.model.metadatacommand.subcommand.AggregateWindowMetricsModel;
import io.littlehorse.common.proto.Command;
import io.littlehorse.common.proto.DeleteMetricWindow;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.MetricWindowId;
import io.littlehorse.sdk.common.proto.Tenant;
import io.littlehorse.sdk.common.proto.WfSpecId;
import io.littlehorse.server.LHServer;
import io.littlehorse.server.monitoring.metrics.CommandProcessorMetrics;
import io.littlehorse.server.streams.ServerTopology;
import io.littlehorse.server.streams.store.LHIterKeyValue;
import io.littlehorse.server.streams.store.LHKeyValueIterator;
import io.littlehorse.server.streams.store.StoredGetable;
import io.littlehorse.server.streams.storeinternals.TaskQueueHintModel;
import io.littlehorse.server.streams.stores.ClusterScopedStore;
import io.littlehorse.server.streams.stores.PartitionLocalBuffer;
import io.littlehorse.server.streams.stores.TenantScopedStore;
import io.littlehorse.server.streams.taskqueue.TaskQueueManager;
import io.littlehorse.server.streams.topology.core.BackgroundContext;
import io.littlehorse.server.streams.topology.core.CommandProcessorOutput;
import io.littlehorse.server.streams.topology.core.CoreCommandException;
import io.littlehorse.server.streams.topology.core.CoreProcessorContext;
import io.littlehorse.server.streams.topology.core.Forwardable;
import io.littlehorse.server.streams.topology.core.LHProcessingExceptionHandler;
import io.littlehorse.server.streams.util.AsyncWaiters;
import io.littlehorse.server.streams.util.MetadataCache;
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
public class CommandProcessor implements Processor<String, Command, String, Forwardable> {

    protected ProcessorContext<String, Forwardable> ctx;
    private final LHServerConfig config;
    private final LHServer server;
    private final MetadataCache metadataCache;
    private final TaskQueueManager globalTaskQueueManager;

    protected KeyValueStore<String, Bytes> nativeStore;
    protected KeyValueStore<String, Bytes> globalStore;
    private boolean partitionIsClaimed;
    private final AsyncWaiters asyncWaiters;

    private final PartitionLocalBuffer<PartitionMetricWindowModel> metricWindows;
    private final PartitionLocalBuffer<PartitionCountedTagModel> countedTags;
    private PartitionDrainScheduler partitionDrain;

    private final LHProcessingExceptionHandler exceptionHandler;
    private final CommandProcessorMetrics metrics;

    public CommandProcessor(
            LHServerConfig config,
            LHServer server,
            MetadataCache metadataCache,
            TaskQueueManager globalTaskQueueManager,
            AsyncWaiters asyncWaiters,
            CommandProcessorMetrics metrics) {
        this.metrics = metrics;
        this.config = config;
        this.server = server;
        this.metadataCache = metadataCache;
        this.globalTaskQueueManager = globalTaskQueueManager;
        this.exceptionHandler = new LHProcessingExceptionHandler(server, asyncWaiters);
        this.asyncWaiters = asyncWaiters;
        this.metricWindows = new PartitionLocalBuffer<>();
        this.countedTags = new PartitionLocalBuffer<>();
    }

    @Override
    public void init(final ProcessorContext<String, Forwardable> ctx) {
        log.info("Starting the init() process on partition {}", ctx.taskId().partition());
        this.ctx = ctx;
        this.nativeStore = ctx.getStateStore(ServerTopology.CORE_STORE);
        this.globalStore = ctx.getStateStore(ServerTopology.GLOBAL_METADATA_STORE);
        this.partitionDrain = new PartitionDrainScheduler(metricWindows, countedTags, config, ctx);
        onPartitionClaimed();
        ctx.schedule(
                LHConstants.PARTITION_METRICS_PUNCTUATOR_INTERVAL,
                PunctuationType.WALL_CLOCK_TIME,
                this::collectPartitionMetrics);

        log.info("Completed the init() process on partition {}", ctx.taskId().partition());
    }

    @Override
    public void process(final Record<String, Command> commandRecord) {
        exceptionHandler.tryRun(() -> processHelper(commandRecord));
        CommandProcessorOutput cpo = new CommandProcessorOutput();
        DeleteMetricWindow hey = DeleteMetricWindow.newBuilder().setId(MetricWindowId.newBuilder().setWfSpecId(WfSpecId.newBuilder().setName("hey").build()).build()).build();
        DeleteMetricWindowModel model = LHSerializable.fromProto(hey, DeleteMetricWindowModel.class, new BackgroundContext());
        cpo.payload = new LHTimer(new CommandModel(model), 2);
        Record<String, CommandProcessorOutput> out = new Record<>(
                "",
                cpo,
                System.currentTimeMillis(),
                HeadersUtil.metadataHeadersFor(new TenantIdModel(LHConstants.DEFAULT_TENANT), new PrincipalIdModel(LHConstants.ANONYMOUS_PRINCIPAL)));
        ctx.forward(out);
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
            metrics.observe(command);
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
                commandToProcess,
                metadataHeaders,
                config,
                ctx,
                globalTaskQueueManager,
                metadataCache,
                server,
                metricWindows,
                countedTags);
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
        if (partitionIsClaimed) {
            this.partitionDrain.reset();
        }
        this.partitionIsClaimed = false;
        server.drainPartitionTaskQueue(ctx.taskId());
    }

    private void collectPartitionMetrics(long timestamp) {
        ClusterScopedStore clusterScopedStore =
                ClusterScopedStore.newInstance(ctx.getStateStore(ServerTopology.CORE_STORE), new BackgroundContext());
        partitionDrain.punctuate(clusterScopedStore);
    }
}
