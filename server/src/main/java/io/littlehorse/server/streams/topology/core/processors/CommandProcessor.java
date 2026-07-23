package io.littlehorse.server.streams.topology.core.processors;

import com.google.protobuf.Message;
import io.grpc.Status;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.PartitionCountedTagModel;
import io.littlehorse.common.model.PartitionMetricWindowModel;
import io.littlehorse.common.model.ScheduledTaskModel;
import io.littlehorse.common.model.corecommand.CommandModel;
import io.littlehorse.common.model.getable.global.acl.TenantModel;
import io.littlehorse.common.proto.Command;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.sdk.common.proto.Tenant;
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
import io.littlehorse.server.streams.topology.core.LHProcessingExceptionHandler;
import io.littlehorse.server.streams.util.AsyncWaiters;
import io.littlehorse.server.streams.util.MetadataCache;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.errors.RecordTooLargeException;
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

    private final PartitionLocalBuffer<PartitionMetricWindowModel> metricWindows;
    private final PartitionLocalBuffer<PartitionCountedTagModel> countedTags;
    private PartitionDrainScheduler partitionDrain;

    private final LHProcessingExceptionHandler exceptionHandler;
    private final CommandProcessorMetrics metrics;
    private BulkJobPunctuator bulkJobPunctuator;
    private static final Duration BULK_JOB_PUNCTUATION_BUDGET = Duration.ofMillis(50);
    private final long bulkJobMaxCommandsPerPunctuation;

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
        this.bulkJobMaxCommandsPerPunctuation = config.getMaxBulkJobCommandsPerTick();
    }

    @Override
    public void init(final ProcessorContext<String, CommandProcessorOutput> ctx) {
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

        this.bulkJobPunctuator = new BulkJobPunctuator(
                ctx, config, metadataCache, BULK_JOB_PUNCTUATION_BUDGET, bulkJobMaxCommandsPerPunctuation);
        ctx.schedule(
                Duration.ofSeconds(1),
                PunctuationType.WALL_CLOCK_TIME,
                timestamp -> bulkJobPunctuator.punctuate(timestamp));

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
        Message response;
        try {
            metrics.observe(command);
            response = command.process(executionContext, config);
            executionContext.endExecution();
            log.debug(
                    "Processed command type {} (commandId {}) on partition {}",
                    command.type,
                    command.getCommandId(),
                    ctx.taskId().partition());
        } catch (RecordTooLargeException e) {
            log.debug(
                    "Command type {} (commandId {}) exceeded record size limit: {}",
                    command.type,
                    command.getCommandId(),
                    e.getMessage());
            throw new CoreCommandException(
                    new LHApiException(Status.RESOURCE_EXHAUSTED.withDescription(e.getMessage()), e), command);
        } catch (KafkaException ke) {
            // Kafka-level failures are typically fatal and retried by Streams; surface them at WARN.
            log.warn(
                    "Kafka error while processing command type {} (commandId {}) on partition {}",
                    command.type,
                    command.getCommandId(),
                    ctx.taskId().partition(),
                    ke);
            throw ke;
        } catch (Exception exn) {
            // Full stack goes to DEBUG to avoid noisy INFO; the exception handler decides final disposition.
            log.debug(
                    "Command type {} (commandId {}) failed on partition {}",
                    command.type,
                    command.getCommandId(),
                    ctx.taskId().partition(),
                    exn);
            throw new CoreCommandException(exn, command);
        }
        if (command.hasResponse()) {
            CompletableFuture<Message> completable = asyncWaiters.getOrRegisterFuture(
                    command.getCommandId().get(), Message.class, new CompletableFuture<>());
            completable.complete(response);
            log.trace("Completed response future for commandId {}", command.getCommandId());
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
        long startMillis = System.currentTimeMillis();
        try (LHKeyValueIterator<?> storedTenants = clusterStore.range(
                GetableClassEnum.TENANT.getNumber() + "/",
                GetableClassEnum.TENANT.getNumber() + "/~",
                StoredGetable.class)) {
            while (storedTenants.hasNext()) {
                TenantModel storedTenant = ((StoredGetable<Tenant, TenantModel>)
                                storedTenants.next().getValue())
                        .getStoredObject();
                rehydrateTenant(storedTenant);
            }
        }
        log.info(
                "Claimed partition {}, rehydration took {}ms",
                ctx.taskId().partition(),
                System.currentTimeMillis() - startMillis);
    }

    private void rehydrateTenant(TenantModel tenant) {
        log.debug("Rehydrating tenant on partition {}", ctx.taskId().partition());
        TenantScopedStore coreDefaultStore =
                TenantScopedStore.newInstance(this.nativeStore, tenant.getId(), new BackgroundContext());

        TaskQueueHintModel hint =
                coreDefaultStore.get(TaskQueueHintModel.TASK_QUEUE_HINT_KEY, TaskQueueHintModel.class);

        if (hint == null) {
            // Expected on cold start; INFO because it signals a potentially expensive tombstone scan.
            log.info(
                    "No task queue hint on partition {}; may iterate over many tombstones",
                    ctx.taskId().partition());
        } else {
            log.debug("Resuming task rehydration from key {}", hint.getKeyToResumeFrom());
        }
        String startKey = hint == null ? "" : hint.getKeyToResumeFrom();
        String endKey = "~";
        int scheduledCount = 0;
        try (LHKeyValueIterator<ScheduledTaskModel> iter =
                coreDefaultStore.range(startKey, endKey, ScheduledTaskModel.class)) {
            while (iter.hasNext()) {
                LHIterKeyValue<ScheduledTaskModel> next = iter.next();
                ScheduledTaskModel scheduledTask = next.getValue();
                log.trace(
                        "Rehydration: scheduling task on partition {}",
                        ctx.taskId().partition());
                // This will break task rehydration for tenant specific test. this will be addressed in Issue #554
                server.onTaskScheduled(
                        ctx.taskId(), scheduledTask.getTaskDefId(), scheduledTask.getTaskRunId(), tenant.getId());
                scheduledCount++;
            }
        }
        log.debug(
                "Rehydrated {} scheduled task(s) on partition {}",
                scheduledCount,
                ctx.taskId().partition());
    }

    @Override
    public void close() {
        log.info("Closing CommandProcessor on partition {}", ctx.taskId().partition());
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
