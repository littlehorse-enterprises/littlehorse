package io.littlehorse.server.streams.topology.core.background;

import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.server.streams.topology.core.CoreStoreProvider;
import java.io.Closeable;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.errors.InvalidStateStoreException;
import org.apache.kafka.streams.processor.TaskId;

/**
 * Owns the single background thread dedicated to one Kafka partition, plus the FIFO queue of action
 * batches that thread produces.
 *
 * <p>The threading contract is the whole point of this class:
 * <ul>
 *   <li>The <b>worker thread</b> runs every {@link PartitionBackgroundJob} in a loop. It may block,
 *       scan and compute freely. It reads state through IQv1 and submits batches of actions.</li>
 *   <li>The <b>Kafka Streams thread</b> only ever calls {@link #drain}, from the single unified
 *       punctuator, applying whole batches in the order they were produced.</li>
 * </ul>
 *
 * <p><b>A batch is the unit of atomicity.</b> Kafka Streams never commits in the middle of a
 * punctuation, so every action in a batch lands in the same transaction. Jobs rely on that: the
 * metrics replay emits two aggregates and a delete per window, and applying only some of those
 * would double-count or lose the window entirely.
 *
 * <p>One instance exists per partition assignment: it is created in {@code Processor.init()} and
 * closed in {@code Processor.close()}. That lifecycle is what makes rebalances safe — on revocation
 * the instance is discarded along with any batches it had queued, so nothing is ever applied to a
 * store this server no longer owns.
 */
@Slf4j
public final class PartitionActionScheduler<VOut> implements Closeable {

    /**
     * Bounded on purpose. A full queue blocks the worker in {@code submit()}, which is exactly the
     * backpressure we want: the background thread must never outrun the punctuator's ability to
     * commit.
     */
    private static final int DEFAULT_QUEUE_CAPACITY = 1_000;

    /**
     * How long the worker sleeps when no job is due.
     */
    private static final Duration IDLE_POLL = Duration.ofMillis(50);

    /**
     * Backoff applied when IQv1 is unavailable (rebalance / state restore).
     */
    private static final Duration STORE_UNAVAILABLE_BACKOFF = Duration.ofSeconds(1);

    private final TaskId taskId;
    private final BlockingQueue<List<PartitionAction<VOut>>> pending;
    private final AtomicInteger pendingActions = new AtomicInteger();
    private final List<PartitionBackgroundJob<VOut>> jobs;
    private final PartitionJobContext<VOut> jobContext;

    private volatile boolean running;
    private volatile boolean closed;
    private Thread worker;

    public PartitionActionScheduler(
            TaskId taskId,
            LHServerConfig config,
            CoreStoreProvider storeProvider,
            List<PartitionBackgroundJob<VOut>> jobs) {
        this(taskId, config, storeProvider, jobs, DEFAULT_QUEUE_CAPACITY);
    }

    PartitionActionScheduler(
            TaskId taskId,
            LHServerConfig config,
            CoreStoreProvider storeProvider,
            List<PartitionBackgroundJob<VOut>> jobs,
            int queueCapacity) {
        this.taskId = taskId;
        this.jobs = List.copyOf(jobs);
        this.pending = new ArrayBlockingQueue<>(queueCapacity);
        this.jobContext = new PartitionJobContext<>(taskId.partition(), config, storeProvider, this);
    }

    /**
     * Starts the worker thread. Must be called from {@code Processor.init()}.
     */
    public void start() {
        if (closed) {
            throw new IllegalStateException("Cannot restart a closed PartitionActionScheduler");
        }
        if (jobs.isEmpty()) {
            log.debug("No background jobs registered for partition {}; not starting a worker", taskId.partition());
            return;
        }
        running = true;
        worker = Thread.ofPlatform()
                .name("lh-partition-worker-" + taskId)
                .daemon(true)
                .unstarted(this::loop);
        worker.start();
        log.info("Started background worker for partition {} with {} job(s)", taskId.partition(), jobs.size());
    }

    /**
     * Called by the worker thread (via {@link PartitionJobContext#submit()}). Blocks when the queue
     * is full.
     */
    void submitBatch(List<PartitionAction<VOut>> batch) throws InterruptedException {
        if (closed) {
            // Partition was revoked while the job was mid-flight; drop the effects on the floor.
            throw new InterruptedException("Partition " + taskId.partition() + " no longer owned");
        }
        pending.put(batch);
        pendingActions.addAndGet(batch.size());
    }

    /**
     * Number of actions waiting to be applied. This is the signal that the worker is outrunning the
     * punctuator, and is also what lets a job implement a barrier: "do not start another scan until
     * everything the previous one produced has taken effect".
     */
    public int pendingActionCount() {
        return pendingActions.get();
    }

    /**
     * Number of batches waiting to be applied.
     */
    public int pendingBatchCount() {
        return pending.size();
    }

    /**
     * Applies queued batches on the Kafka Streams thread, in FIFO order, until the queue is empty or
     * the budget runs out. Whatever is left over is picked up by the next punctuation.
     *
     * <p>The budget is only ever evaluated <i>between</i> batches, never inside one — that is what
     * keeps a job's atomic unit inside a single transaction. At least one batch is always applied,
     * so a batch larger than the budget cannot stall the queue forever.
     *
     * @return the number of actions applied.
     */
    public int drain(PartitionActionApplier<VOut> applier, Duration budget, int maxActions) {
        Instant deadline = Instant.now().plus(budget);
        int applied = 0;
        while (true) {
            if (applied > 0 && (applied >= maxActions || Instant.now().isAfter(deadline))) {
                break;
            }
            List<PartitionAction<VOut>> batch = pending.peek();
            if (batch == null) {
                break;
            }
            // Dequeue only once every action has landed. If one throws, the exception leaves
            // drain(), the punctuation fails and Kafka Streams aborts the transaction; the batch is
            // still queued, so it is retried next tick against the rolled-back state.
            for (PartitionAction<VOut> action : batch) {
                action.apply(applier);
            }
            pending.poll();
            pendingActions.addAndGet(-batch.size());
            applied += batch.size();
        }
        if (applied > 0 && !pending.isEmpty()) {
            log.debug(
                    "Partition {}: applied {} actions, {} batches still queued",
                    taskId.partition(),
                    applied,
                    pending.size());
        }
        return applied;
    }

    @Override
    public void close() {
        closed = true;
        running = false;
        if (worker != null) {
            worker.interrupt();
            try {
                worker.join(TimeUnit.SECONDS.toMillis(5));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        // Anything still queued belongs to a partition we no longer own.
        List<List<PartitionAction<VOut>>> discarded = new ArrayList<>();
        pending.drainTo(discarded);
        pendingActions.set(0);
        if (!discarded.isEmpty()) {
            log.info(
                    "Discarded {} pending action batches on revocation of partition {}",
                    discarded.size(),
                    taskId.partition());
        }
    }

    boolean isWorkerAlive() {
        return worker != null && worker.isAlive();
    }

    private void loop() {
        Map<String, Instant> nextRunAt = new HashMap<>();
        jobs.forEach(job -> nextRunAt.put(job.name(), Instant.now()));

        while (running && !Thread.currentThread().isInterrupted()) {
            try {
                boolean ranSomething = false;
                for (PartitionBackgroundJob<VOut> job : jobs) {
                    if (!running) {
                        break;
                    }
                    if (Instant.now().isBefore(nextRunAt.get(job.name()))) {
                        continue;
                    }
                    ranSomething = true;
                    runOnce(job);
                    nextRunAt.put(job.name(), Instant.now().plus(job.interval()));
                }
                if (!ranSomething) {
                    Thread.sleep(IDLE_POLL.toMillis());
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        jobs.forEach(PartitionBackgroundJob::onPartitionRevoked);
        log.info("Background worker for partition {} exiting", taskId.partition());
    }

    private void runOnce(PartitionBackgroundJob<VOut> job) throws InterruptedException {
        try {
            job.run(jobContext);
            // Submit whatever the run staged but did not submit itself.
            jobContext.submit();
        } catch (InterruptedException e) {
            jobContext.discardStaged();
            throw e;
        } catch (InvalidStateStoreException | LHApiException e) {
            // Kafka Streams is rebalancing or restoring; this is expected and transient.
            jobContext.discardStaged();
            log.debug("IQ store unavailable for job {} on partition {}; backing off", job.name(), taskId.partition());
            Thread.sleep(STORE_UNAVAILABLE_BACKOFF.toMillis());
        } catch (Exception e) {
            // A run that failed part-way through produces nothing, rather than a half-finished unit.
            int dropped = jobContext.stagedCount();
            jobContext.discardStaged();
            log.error(
                    "Background job {} failed on partition {}, discarding {} staged action(s)",
                    job.name(),
                    taskId.partition(),
                    dropped,
                    e);
        }
    }
}
