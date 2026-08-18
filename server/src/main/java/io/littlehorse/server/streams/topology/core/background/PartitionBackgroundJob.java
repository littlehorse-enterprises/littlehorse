package io.littlehorse.server.streams.topology.core.background;

import java.time.Duration;

/**
 * A unit of periodic work that used to live in its own Punctuator, and now runs on the
 * per-partition worker thread owned by {@link PartitionActionScheduler}.
 *
 * <p>Implementations may block, scan large key ranges and take as long as they need: they are NOT
 * on the Kafka Streams thread, so they can neither stall processing nor blow up the transaction
 * timeout. What they may NOT do is touch a state store or the ProcessorContext directly — see
 * {@link PartitionJobContext}.
 *
 * <p>Jobs must be interruption-aware: on partition revocation the worker thread is interrupted, and
 * a job is expected to unwind promptly by propagating {@link InterruptedException}.
 */
public interface PartitionBackgroundJob {

    /**
     * Used for logging/metrics.
     */
    String name();

    /**
     * How often this job wants to run. The worker executes jobs sequentially, so a slow job delays
     * the ones after it; this is a floor, not a guarantee.
     */
    Duration interval();

    /**
     * Runs one tick of the job.
     */
    void run(PartitionJobContext ctx) throws InterruptedException;

    /**
     * Called on the worker thread when the partition is being revoked, so the job can drop any
     * in-memory cursor state. Actions already enqueued are discarded by the scheduler.
     */
    default void onPartitionRevoked() {}
}
