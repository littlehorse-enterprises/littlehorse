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
import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.proto.Tenant;
import io.littlehorse.server.LHServer;
import io.littlehorse.server.monitoring.metrics.CommandProcessorMetrics;
import io.littlehorse.server.streams.ServerTopology;
import io.littlehorse.server.streams.store.LHIterKeyValue;
import io.littlehorse.server.streams.store.LHKeyValueIterator;
import io.littlehorse.server.streams.store.StoredGetable;
import io.littlehorse.server.streams.storeinternals.TaskQueueHintModel;
import io.littlehorse.server.streams.storeinternals.TimerIteratorHintModel;
import io.littlehorse.server.streams.stores.ClusterScopedStore;
import io.littlehorse.server.streams.stores.PartitionLocalBuffer;
import io.littlehorse.server.streams.stores.TenantScopedStore;
import io.littlehorse.server.streams.taskqueue.TaskQueueManager;
import io.littlehorse.server.streams.topology.core.BackgroundContext;
import io.littlehorse.server.streams.topology.core.CommandProcessorOutput;
import io.littlehorse.server.streams.topology.core.CoreCommandException;
import io.littlehorse.server.streams.topology.core.CoreProcessorContext;
import io.littlehorse.server.streams.topology.core.LHProcessingExceptionHandler;
import io.littlehorse.server.streams.topology.core.background.PartitionActionApplier;
import io.littlehorse.server.streams.topology.core.background.PartitionActionScheduler;
import io.littlehorse.server.streams.topology.core.background.PartitionBackgroundJob;
import io.littlehorse.server.streams.util.AsyncWaiters;
import io.littlehorse.server.streams.util.MetadataCache;
import java.time.Duration;
import java.util.List;
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

    /**
     * The single punctuation tick for this processor. Everything that used to have its own
     * {@code ctx.schedule()} call now runs from here, gated by its own cadence.
     */
    private static final Duration PUNCTUATION_TICK = Duration.ofMillis(100);

    /** Budget for applying actions produced by the per-partition background worker. */
    private static final Duration ACTION_DRAIN_BUDGET = Duration.ofMillis(50);

    private static final int MAX_ACTIONS_PER_PUNCTUATION = 500;

    private PartitionActionScheduler<CommandProcessorOutput> backgroundScheduler;
    private PartitionMetricsCatchUpJob metricsCatchUpJob;
    private BulkJobScanJob bulkJobScanJob;
    private TimerScanJob timerScanJob;

    /**
     * Resume point of the timer scan. Owned here rather than by {@code TimerCoreProcessor} because
     * this processor owns the task's only background thread, and the scan runs on it.
     */
    private final TimerCursor timerCursor = new TimerCursor();

    /**
     * Cadence gate for the only work that still runs inline on the Kafka Streams thread. It is driven
     * by the punctuation timestamp rather than {@code Instant.now()} so the schedule is a pure
     * function of the tick, which keeps it deterministic under test.
     *
     * <p>It exists because the unified tick ({@link #PUNCTUATION_TICK}) is much faster than the
     * cadence this work wants; without it the drain would run 30x more often than it did when it had
     * its own {@code ctx.schedule()}, and would forward correspondingly smaller batches.
     */
    private long nextMetricsRunAt = Long.MIN_VALUE;

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
    public void init(final ProcessorContext<String, CommandProcessorOutput> ctx) {
        log.info("Starting the init() process on partition {}", ctx.taskId().partition());
        this.ctx = ctx;
        this.nativeStore = ctx.getStateStore(ServerTopology.CORE_STORE);
        this.globalStore = ctx.getStateStore(ServerTopology.GLOBAL_METADATA_STORE);
        this.metricsCatchUpJob = new PartitionMetricsCatchUpJob(config);
        this.bulkJobScanJob = new BulkJobScanJob(config);
        this.timerScanJob = new TimerScanJob(timerCursor, config);
        seedTimerCursor();
        this.partitionDrain =
                new PartitionDrainScheduler(metricWindows, countedTags, config, ctx, metricsCatchUpJob::isComplete);
        onPartitionClaimed();

        // The per-partition worker thread. Jobs registered here run OFF the Streams thread and
        // communicate back exclusively through PartitionActions.
        this.backgroundScheduler =
                new PartitionActionScheduler<>(ctx.taskId(), config, server.getCoreStoreProvider(), backgroundJobs());
        this.backgroundScheduler.start();

        // A single punctuator to rule them all.
        ctx.schedule(PUNCTUATION_TICK, PunctuationType.WALL_CLOCK_TIME, this::punctuate);

        log.info("Completed the init() process on partition {}", ctx.taskId().partition());
    }

    /**
     * All periodic work that does not need the Kafka Streams thread.
     */
    private List<PartitionBackgroundJob<CommandProcessorOutput>> backgroundJobs() {
        return List.of(metricsCatchUpJob, bulkJobScanJob, timerScanJob);
    }

    /**
     * Picks the timer scan back up where the previous owner of this partition left off. Without the
     * hint the first scan would have to walk every tombstone left by every timer ever delivered.
     *
     * <p>Seeds unconditionally: a cursor carried over from a partition this instance used to own
     * would silently skip every timer already matured on the new one.
     */
    private void seedTimerCursor() {
        ClusterScopedStore coreStore = ClusterScopedStore.newInstance(nativeStore, new BackgroundContext());
        TimerIteratorHintModel hint =
                coreStore.get(TimerIteratorHintModel.TIMER_ITERATOR_HINT_KEY, TimerIteratorHintModel.class);
        timerCursor.seed(
                hint == null
                        ? 0L
                        : LHLibUtil.fromProtoTs(hint.getLastProcessedTimer()).getTime());
    }

    /**
     * The unified punctuation. Runs on the Kafka Streams thread and does two things, in order:
     *
     * <ol>
     *   <li>Applies whatever the background worker produced since the last tick, under a strict
     *       time/count budget so a busy worker can never blow the transaction timeout.</li>
     *   <li>Flushes the partition-local buffers, which cannot leave this thread.</li>
     * </ol>
     */
    private void punctuate(long timestamp) {
        backgroundScheduler.drain(
                new PartitionActionApplier<>(ctx, nativeStore), ACTION_DRAIN_BUDGET, MAX_ACTIONS_PER_PUNCTUATION);

        if (timestamp >= nextMetricsRunAt) {
            nextMetricsRunAt = timestamp + LHConstants.PARTITION_METRICS_PUNCTUATOR_INTERVAL.toMillis();
            drainPartitionLocalBuffers();
        }
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
        } catch (RecordTooLargeException e) {
            throw new CoreCommandException(
                    new LHApiException(Status.RESOURCE_EXHAUSTED.withDescription(e.getMessage()), e), command);
        } catch (KafkaException ke) {
            throw ke;
        } catch (Exception exn) {
            throw new CoreCommandException(exn, command);
        }
        if (command.hasResponse()) {
            CompletableFuture<Message> completable = asyncWaiters.getOrRegisterFuture(
                    command.getCommandId().get(), Message.class, new CompletableFuture<>());
            completable.complete(response);
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
        if (backgroundScheduler != null) {
            backgroundScheduler.close();
            backgroundScheduler = null;
        }
        if (partitionIsClaimed) {
            this.partitionDrain.reset();
        }
        this.partitionIsClaimed = false;
        server.drainPartitionTaskQueue(ctx.taskId());
    }

    /**
     * Flushes the partition-local metric and counted-tag buffers. Must stay on the Kafka Streams
     * thread: the buffers it reads are written by {@code process()} and are not thread safe.
     */
    private void drainPartitionLocalBuffers() {
        ClusterScopedStore clusterScopedStore =
                ClusterScopedStore.newInstance(ctx.getStateStore(ServerTopology.CORE_STORE), new BackgroundContext());
        partitionDrain.punctuate(clusterScopedStore);
    }
}
