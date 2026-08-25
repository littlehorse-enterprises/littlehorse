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
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.errors.InvalidStateStoreException;
import org.apache.kafka.streams.processor.TaskId;

/**
 * Owns the single background thread dedicated to one Kafka partition, plus the FIFO queue of
 * {@link PartitionAction}s that thread produces.
 *
 * <p>The threading contract is the whole point of this class:
 * <ul>
 *   <li>The <b>worker thread</b> runs every {@link PartitionBackgroundJob} in a loop. It may block,
 *       scan and compute freely. It reads state through IQv1 and produces actions.</li>
 *   <li>The <b>Kafka Streams thread</b> only ever calls {@link #drain}, from the single unified
 *       punctuator, applying actions in the order they were produced under a time/count budget.</li>
 * </ul>
 *
 * <p>One instance exists per partition assignment: it is created in {@code Processor.init()} and
 * closed in {@code Processor.close()}. That lifecycle is what makes rebalances safe — on revocation
 * the instance is discarded along with any actions it had queued, so nothing is ever applied to a
 * store this server no longer owns.
 */
@Slf4j
public final class PartitionActionScheduler<VOut> implements Closeable {

    /**
     * Bounded on purpose. A full queue blocks the worker, which is exactly the backpressure we want:
     * the background thread must never be allowed to outrun the punctuator's ability to commit.
     */
    private static final int DEFAULT_QUEUE_CAPACITY = 10_000;

    /**
     * How long the worker sleeps when no job is due.
     */
    private static final Duration IDLE_POLL = Duration.ofMillis(50);

    /**
     * Backoff applied when IQv1 is unavailable (rebalance / state restore).
     */
    private static final Duration STORE_UNAVAILABLE_BACKOFF = Duration.ofSeconds(1);

    private final TaskId taskId;
    private final BlockingQueue<PartitionAction<VOut>> pending;
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
     * Called by the worker thread (via {@link PartitionJobContext}). Blocks when the queue is full.
     */
    void enqueue(PartitionAction<VOut> action) throws InterruptedException {
        if (closed) {
            // Partition was revoked while the job was mid-flight; drop the effect on the floor.
            throw new InterruptedException("Partition " + taskId.partition() + " no longer owned");
        }
        pending.put(action);
    }

    /**
     * Number of actions waiting to be applied. This is the signal that the worker is outrunning the
     * punctuator, and is also what lets a job implement a barrier: "do not start another scan until
     * everything the previous one produced has taken effect".
     */
    public int pendingCount() {
        return pending.size();
    }

    /**
     * Applies queued actions on the Kafka Streams thread, in FIFO order, until the queue is empty or
     * the budget runs out. Whatever is left over is picked up by the next punctuation.
     *
     * @return the number of actions applied.
     */
    public int drain(PartitionActionApplier<VOut> applier, Duration budget, int maxActions) {
        Instant deadline = Instant.now().plus(budget);
        int applied = 0;
        while (applied < maxActions) {
            PartitionAction<VOut> action = pending.poll();
            if (action == null) {
                break;
            }
            try {
                action.apply(applier);
            } catch (Exception e) {
                // An action must never take down the Streams thread. Log loudly and keep going;
                // jobs are expected to be idempotent so the work will be redone next tick.
                log.error("Failed to apply {} on partition {}", action.describe(), taskId.partition(), e);
            }
            applied++;
            if (Instant.now().isAfter(deadline)) {
                break;
            }
        }
        if (applied == maxActions || !pending.isEmpty()) {
            log.debug("Partition {}: applied {} actions, {} still queued", taskId.partition(), applied, pending.size());
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
        List<PartitionAction<VOut>> discarded = new ArrayList<>();
        pending.drainTo(discarded);
        if (!discarded.isEmpty()) {
            log.info(
                    "Discarded {} pending actions on revocation of partition {}", discarded.size(), taskId.partition());
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
        } catch (InterruptedException e) {
            throw e;
        } catch (UncheckedInterruptedException e) {
            throw e.getCause();
        } catch (InvalidStateStoreException | LHApiException e) {
            // Kafka Streams is rebalancing or restoring; this is expected and transient.
            log.debug("IQ store unavailable for job {} on partition {}; backing off", job.name(), taskId.partition());
            Thread.sleep(STORE_UNAVAILABLE_BACKOFF.toMillis());
        } catch (Exception e) {
            log.error("Background job {} failed on partition {}", job.name(), taskId.partition(), e);
        }
    }
}
