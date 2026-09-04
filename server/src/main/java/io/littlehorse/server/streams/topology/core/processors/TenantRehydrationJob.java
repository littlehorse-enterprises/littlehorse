package io.littlehorse.server.streams.topology.core.processors;

import io.littlehorse.common.model.ScheduledTaskModel;
import io.littlehorse.common.model.getable.global.acl.TenantModel;
import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.sdk.common.proto.Tenant;
import io.littlehorse.server.LHServer;
import io.littlehorse.server.streams.store.LHKeyValueIterator;
import io.littlehorse.server.streams.store.StoredGetable;
import io.littlehorse.server.streams.storeinternals.TaskQueueHintModel;
import io.littlehorse.server.streams.stores.ReadOnlyTenantScopedStore;
import io.littlehorse.server.streams.topology.core.CommandProcessorOutput;
import io.littlehorse.server.streams.topology.core.background.PartitionBackgroundJob;
import io.littlehorse.server.streams.topology.core.background.PartitionJobContext;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.processor.TaskId;

/**
 * Rebuilds the in-memory task queue for this partition after a restart or rebalance.
 *
 * <p>The task queue is in-memory only, so on claiming a partition the server has to walk every
 * scheduled-but-unstarted task in the store and re-offer it to workers. This used to run inline in
 * {@code CommandProcessor.init()}, which meant a partition with a large backlog could not begin
 * processing commands until the whole scan finished.
 *
 * <p>Three properties of the task queue make it safe to do this off the Streams thread, and to
 * interleave it with live command processing:
 *
 * <ul>
 *   <li>{@code OneTaskQueue.onTaskScheduled} is guarded by a {@code ReentrantLock}, so it may be
 *       called from this worker.</li>
 *   <li>Queue items are de-duplicated by {@code TaskRunId}, so re-offering a task that live
 *       processing has already offered is a no-op. This is also what lets a partial pass resume by
 *       re-reading its last key.</li>
 *   <li>A worker never executes straight off an offer: it must claim the task through a
 *       {@code TaskClaimEvent}, which the Streams thread validates against the authoritative store.
 *       An offer for a task that has since completed therefore yields an empty poll response rather
 *       than a duplicate execution — the same path already used when two workers race.</li>
 * </ul>
 *
 * <p>The scan yields on a time budget because this worker is shared with the timer scan; an
 * unbounded rehydration would delay timer delivery for the whole task.
 */
@Slf4j
class TenantRehydrationJob implements PartitionBackgroundJob<CommandProcessorOutput> {

    private static final Duration INTERVAL = Duration.ofMillis(200);

    /**
     * How long one pass may spend scanning before yielding the worker to the other jobs.
     */
    private static final Duration DEFAULT_BUDGET = Duration.ofMillis(500);

    private static final String END_OF_RANGE = "~";

    private final TaskId taskId;
    private final LHServer server;
    private final Duration budget;
    private final AtomicBoolean complete = new AtomicBoolean(false);

    /**
     * Tenants still to rehydrate. Null until the first pass loads them. Only ever touched by the
     * worker thread.
     */
    private Deque<TenantIdModel> pendingTenants;

    /**
     * Where to resume inside the tenant at the head of {@link #pendingTenants}. Null means "not
     * started yet", which is the signal to read that tenant's persisted hint.
     */
    private String resumeKey;

    TenantRehydrationJob(TaskId taskId, LHServer server) {
        this(taskId, server, DEFAULT_BUDGET);
    }

    TenantRehydrationJob(TaskId taskId, LHServer server, Duration budget) {
        this.taskId = taskId;
        this.server = server;
        this.budget = budget;
    }

    /**
     * True once every tenant has been swept. Rehydration is a one-shot: after this the queue is kept
     * current by {@code LHTaskManager} as commands are processed.
     */
    boolean isComplete() {
        return complete.get();
    }

    @Override
    public String name() {
        return "tenant-rehydration";
    }

    @Override
    public Duration interval() {
        return INTERVAL;
    }

    @Override
    public void run(PartitionJobContext<CommandProcessorOutput> ctx) throws InterruptedException {
        if (complete.get()) {
            return;
        }
        Instant deadline = Instant.now().plus(budget);

        if (pendingTenants == null) {
            pendingTenants = loadTenants(ctx);
            log.info(
                    "Rehydrating task queue for partition {} across {} tenant(s)",
                    ctx.partition(),
                    pendingTenants.size());
        }

        while (!pendingTenants.isEmpty()) {
            if (!rehydrateTenant(ctx, pendingTenants.peek(), deadline)) {
                // Out of budget mid-tenant; the next pass picks up from resumeKey.
                return;
            }
            pendingTenants.poll();
        }

        if (complete.compareAndSet(false, true)) {
            log.info("Task queue rehydration complete for partition {}", ctx.partition());
        }
    }

    /**
     * @return true if this tenant was swept to the end, false if the budget ran out first.
     */
    private boolean rehydrateTenant(
            PartitionJobContext<CommandProcessorOutput> ctx, TenantIdModel tenantId, Instant deadline)
            throws InterruptedException {
        ReadOnlyTenantScopedStore store = ctx.coreStore(tenantId);

        if (resumeKey == null) {
            TaskQueueHintModel hint = store.get(TaskQueueHintModel.TASK_QUEUE_HINT_KEY, TaskQueueHintModel.class);
            if (hint == null) {
                log.warn(
                        "No task queue hint for tenant {} on partition {}; may need to iterate over many tombstones",
                        tenantId,
                        ctx.partition());
            }
            resumeKey = hint == null ? "" : hint.getKeyToResumeFrom();
        }

        try (LHKeyValueIterator<ScheduledTaskModel> iter =
                store.range(resumeKey, END_OF_RANGE, ScheduledTaskModel.class)) {
            while (iter.hasNext()) {
                if (Thread.currentThread().isInterrupted()) {
                    // Partition revoked. Stop immediately rather than offering tasks for a partition
                    // whose queue close() is about to drain.
                    throw new InterruptedException("Partition " + ctx.partition() + " revoked during rehydration");
                }
                ScheduledTaskModel scheduledTask = iter.next().getValue();
                log.debug("Rehydration: scheduling task {}", scheduledTask.getStoreKey());
                server.onTaskScheduled(taskId, scheduledTask.getTaskDefId(), scheduledTask.getTaskRunId(), tenantId);

                if (Instant.now().isAfter(deadline)) {
                    resumeKey = successorOf(scheduledTask.getStoreKey());
                    return false;
                }
            }
        }
        resumeKey = null;
        return true;
    }

    /**
     * The next key after {@code key} in lexicographic order. Range starts are inclusive, so without
     * this a pass that yields immediately after its first task would resume on that same task and
     * never make progress.
     */
    private static String successorOf(String key) {
        return key + '\u0000';
    }

    @SuppressWarnings("unchecked")
    private Deque<TenantIdModel> loadTenants(PartitionJobContext<CommandProcessorOutput> ctx) {
        Deque<TenantIdModel> tenants = new ArrayDeque<>();
        try (LHKeyValueIterator<?> storedTenants = ctx.metadataStore()
                .range(
                        GetableClassEnum.TENANT.getNumber() + "/",
                        GetableClassEnum.TENANT.getNumber() + "/~",
                        StoredGetable.class)) {
            storedTenants.forEachRemaining(getable -> {
                TenantModel storedTenant = ((StoredGetable<Tenant, TenantModel>) getable.getValue()).getStoredObject();
                tenants.add(storedTenant.getId());
            });
        }
        return tenants;
    }
}
