package io.littlehorse.server.streams.topology.core.processors;

import static com.google.protobuf.util.Timestamps.fromMillis;
import static com.google.protobuf.util.Timestamps.toMillis;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.model.LHTimer;
import io.littlehorse.common.model.PartitionMetricWindowModel;
import io.littlehorse.common.model.ScheduledTaskModel;
import io.littlehorse.common.model.corecommand.CommandModel;
import io.littlehorse.common.model.getable.global.acl.TenantModel;
import io.littlehorse.common.model.getable.objectId.PrincipalIdModel;
import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import io.littlehorse.common.model.metadatacommand.subcommand.AggregateWindowMetricsModel;
import io.littlehorse.common.proto.Command;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.Tenant;
import io.littlehorse.server.LHServer;
import io.littlehorse.server.streams.ServerTopology;
import io.littlehorse.server.streams.store.LHIterKeyValue;
import io.littlehorse.server.streams.store.LHKeyValueIterator;
import io.littlehorse.server.streams.store.StoredGetable;
import io.littlehorse.server.streams.storeinternals.MetricsHintModel;
import io.littlehorse.server.streams.storeinternals.TaskQueueHintModel;
import io.littlehorse.server.streams.stores.ClusterScopedStore;
import io.littlehorse.server.streams.stores.PartitionMetricsMemoryStore;
import io.littlehorse.server.streams.stores.TenantScopedStore;
import io.littlehorse.server.streams.taskqueue.TaskQueueManager;
import io.littlehorse.server.streams.topology.core.BackgroundContext;
import io.littlehorse.server.streams.topology.core.CommandProcessorOutput;
import io.littlehorse.server.streams.topology.core.CoreCommandException;
import io.littlehorse.server.streams.topology.core.CoreProcessorContext;
import io.littlehorse.server.streams.topology.core.LHProcessingExceptionHandler;
import io.littlehorse.server.streams.util.AsyncWaiters;
import io.littlehorse.server.streams.util.HeadersUtil;
import io.littlehorse.server.streams.util.MetadataCache;
import java.util.Date;
import java.util.Iterator;
import java.util.concurrent.CompletableFuture;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.processor.PunctuationType;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.state.KeyValueStore;

public class CommandProcessor implements Processor<String, Command, String, CommandProcessorOutput> {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CommandProcessor.class);
    protected ProcessorContext<String, CommandProcessorOutput> ctx;
    private final LHServerConfig config;
    private final LHServer server;
    private final MetadataCache metadataCache;
    private final TaskQueueManager globalTaskQueueManager;

    protected KeyValueStore<String, Bytes> nativeStore;
    protected KeyValueStore<String, Bytes> globalStore;
    private boolean partitionIsClaimed;
    private boolean shouldUseMetricsHint;
    private long serverStartWindowTime;
    private final AsyncWaiters asyncWaiters;
    private final PartitionMetricsMemoryStore partitionMetricsMemoryStore;

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
        this.partitionMetricsMemoryStore = new PartitionMetricsMemoryStore();
    }

    @Override
    public void init(final ProcessorContext<String, CommandProcessorOutput> ctx) {
        log.info("Starting the init() process on partition {}", ctx.taskId().partition());
        this.ctx = ctx;
        this.nativeStore = ctx.getStateStore(ServerTopology.CORE_STORE);
        this.globalStore = ctx.getStateStore(ServerTopology.GLOBAL_METADATA_STORE);
        this.shouldUseMetricsHint = true;
        this.serverStartWindowTime = LHUtil.getCurrentWindowDate().getTime() - 1;
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
                commandToProcess,
                metadataHeaders,
                config,
                ctx,
                globalTaskQueueManager,
                metadataCache,
                server,
                partitionMetricsMemoryStore);
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
        this.shouldUseMetricsHint = true;
        server.drainPartitionTaskQueue(ctx.taskId());
    }

    private void collectPartitionMetrics(long timestamp) {
        ClusterScopedStore clusterScopedStore =
                ClusterScopedStore.newInstance(ctx.getStateStore(ServerTopology.CORE_STORE), new BackgroundContext());
        long lastWindowTime = LHUtil.getCurrentWindowDate().getTime() - (2 * 60 * 1000L);
        if (shouldUseMetricsHint) {
            lastWindowTime = collectAndSendStoreMetrics(clusterScopedStore);
        } else {
            collectAndSendMemoryMetrics(clusterScopedStore);
        }
        clusterScopedStore.put(new MetricsHintModel(fromMillis(lastWindowTime)));
    }

    private void collectAndSendMemoryMetrics(ClusterScopedStore store) {
        if (!partitionMetricsMemoryStore.hasEntries()) {
            return;
        }
        long startTime = System.currentTimeMillis();
        Date currentWindow = LHUtil.getCurrentWindowDate();
        Iterator<PartitionMetricWindowModel> iterator =
                partitionMetricsMemoryStore.values().iterator();
        while (iterator.hasNext()) {
            PartitionMetricWindowModel metric = iterator.next();
            if (metric.getId().getWindowStart().before(currentWindow)) {
                forwardAndDeleteFromStore(store, metric);
                iterator.remove();
                if (System.currentTimeMillis() - startTime > LHConstants.MAX_MS_PER_PARTITION_METRICS_PUNCTUATION) {
                    break;
                }
            }
        }
    }

    private long collectAndSendStoreMetrics(ClusterScopedStore store) {
        shouldUseMetricsHint = false;
        MetricsHintModel hint = store.get(MetricsHintModel.METRICS_HINT_KEY, MetricsHintModel.class);
        if (hint == null || hint.getLastProcessedTimestamp() == null) {
            return this.serverStartWindowTime;
        }

        long lastSeenWindowTime = toMillis(hint.getLastProcessedTimestamp());
        String startPrefix = LHConstants.PARTITION_METRICS_KEY + "/" + lastSeenWindowTime;
        String endPrefix = LHConstants.PARTITION_METRICS_KEY + "/" + serverStartWindowTime;
        long startTime = System.currentTimeMillis();
        try (LHKeyValueIterator<PartitionMetricWindowModel> iter =
                store.range(startPrefix, endPrefix, PartitionMetricWindowModel.class)) {
            while (iter.hasNext()) {
                PartitionMetricWindowModel windowMetrics = iter.next().getValue();
                if (windowMetrics != null) {
                    lastSeenWindowTime = forwardAndDeleteFromStore(store, windowMetrics);
                    if (System.currentTimeMillis() - startTime > LHConstants.MAX_MS_PER_PARTITION_METRICS_PUNCTUATION) {
                        shouldUseMetricsHint = true;
                        log.warn(
                                "Hint will be used for next punctuation  from: {} to: {} elapsedTime: {} ms",
                                new Date(lastSeenWindowTime),
                                new Date(serverStartWindowTime),
                                System.currentTimeMillis() - startTime);
                        return lastSeenWindowTime;
                    }
                }
            }
        }
        return lastSeenWindowTime;
    }

    private long forwardAndDeleteFromStore(ClusterScopedStore store, PartitionMetricWindowModel metric) {
        AggregateWindowMetricsModel aggregate = new AggregateWindowMetricsModel(metric);
        fordwardWindowMetrics(aggregate);
        store.delete(metric);
        metric.getId().markAsTenantMetricId();
        fordwardWindowMetrics(aggregate);
        return metric.getId().getWindowStart().getTime();
    }

    private void fordwardWindowMetrics(AggregateWindowMetricsModel agregateMetricsSubcomand) {
        CommandModel command = new CommandModel(agregateMetricsSubcomand, new Date());
        TenantIdModel tenantId =
                agregateMetricsSubcomand.getMetricWindow().getId().getTenantId();
        LHTimer timer = new LHTimer(command);
        timer.maturationTime = new Date();
        timer.topic = this.config.getCoreCmdTopicName();
        timer.setRepartition(true);
        timer.setTenantId(tenantId);
        CommandProcessorOutput cpo = new CommandProcessorOutput();
        cpo.partitionKey = agregateMetricsSubcomand.getPartitionKey();
        cpo.topic = this.config.getCoreCmdTopicName();
        cpo.payload = timer;
        Record<String, CommandProcessorOutput> out = new Record<>(
                cpo.partitionKey,
                cpo,
                System.currentTimeMillis(),
                HeadersUtil.metadataHeadersFor(tenantId, new PrincipalIdModel(LHConstants.ANONYMOUS_PRINCIPAL)));
        this.ctx.forward(out);
    }
}
