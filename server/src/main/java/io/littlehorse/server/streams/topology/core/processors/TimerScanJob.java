package io.littlehorse.server.streams.topology.core.processors;

import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.model.LHTimer;
import io.littlehorse.server.streams.store.LHKeyValueIterator;
import io.littlehorse.server.streams.topology.core.CommandProcessorOutput;
import io.littlehorse.server.streams.topology.core.background.PartitionBackgroundJob;
import io.littlehorse.server.streams.topology.core.background.PartitionJobContext;
import java.time.Duration;
import java.time.Instant;
import lombok.extern.slf4j.Slf4j;

/**
 * Finds matured timers for one partition and schedules their delivery.
 *
 * <p>This was the {@code clearTimers} punctuator on {@code TimerCoreProcessor}, which ran the range
 * scan inline on the Kafka Streams thread and had to stop after 50ms to avoid blowing the
 * transaction timeout. On a partition with a large backlog that cap meant the scan could not keep
 * up, and timers fired late. Running off-thread lets the scan take a much larger budget while the
 * Streams thread only pays for applying the results.
 *
 * <p>The job is stateless apart from the shared {@link TimerCursor}: it re-derives everything from
 * the cursor on each tick, so being interrupted mid-scan simply means the range is swept again.
 * That re-sweep is harmless because {@link MatureTimerAction} is idempotent.
 */
@Slf4j
class TimerScanJob implements PartitionBackgroundJob<CommandProcessorOutput> {

    private static final Duration INTERVAL = Duration.ofMillis(100);

    /**
     * How long one pass may scan. Unlike the old punctuator budget this does not protect a Kafka
     * transaction; it bounds how long an IQ iterator stays open and how stale the scheduled
     * deliveries can get before the punctuator applies them.
     */
    private static final Duration SCAN_BUDGET = Duration.ofMillis(500);

    /**
     * Caps how many deliveries one pass may enqueue, so a huge backlog cannot fill the scheduler's
     * bounded queue and block the worker for a long time on a single tick.
     */
    private static final int MAX_TIMERS_PER_SCAN = 2_000;

    private final TimerCursor cursor;
    private final String timerTopic;

    TimerScanJob(TimerCursor cursor, LHServerConfig config) {
        this.cursor = cursor;
        this.timerTopic = config.getTimerTopic();
    }

    @Override
    public String name() {
        return "timer-scan";
    }

    @Override
    public Duration interval() {
        return INTERVAL;
    }

    @Override
    public void run(PartitionJobContext<CommandProcessorOutput> ctx) throws InterruptedException {
        if (ctx.pendingActions() > 0) {
            // Barrier. The previous pass's deliveries, and the cursor advance that follows them,
            // have not been applied yet. Scanning now would re-read the same range from an even
            // staler view and duplicate work that is already in flight.
            return;
        }

        final long from = cursor.get();
        final long scanEnd = System.currentTimeMillis();
        if (scanEnd <= from) {
            return;
        }

        final Instant deadline = Instant.now().plus(SCAN_BUDGET);

        // Claim the whole window by default; narrowed below if we stop early.
        long proposedCursor = scanEnd;
        int scheduled = 0;

        try (LHKeyValueIterator<LHTimer> iter = ctx.coreStore()
                .range(TimerCursor.storeKeyBound(from), TimerCursor.storeKeyBound(scanEnd), LHTimer.class)) {
            while (iter.hasNext()) {
                LHTimer timer = iter.next().getValue();
                ctx.schedule(new MatureTimerAction(timer, timerTopic));
                scheduled++;

                if (scheduled >= MAX_TIMERS_PER_SCAN || Instant.now().isAfter(deadline)) {
                    // Only claim as far as we actually got, so the rest of the window is swept next
                    // tick instead of being skipped.
                    proposedCursor = timer.getMaturationTime().getTime();
                    log.debug(
                            "Timer scan on partition {} yielding after {} timers, resuming from {}",
                            ctx.partition(),
                            scheduled,
                            proposedCursor);
                    break;
                }
            }
        }

        ctx.schedule(new TimerCursorAdvanceAction(cursor, proposedCursor));
    }
}
