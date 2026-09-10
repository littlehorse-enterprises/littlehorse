package io.littlehorse.server.streams.topology.core.processors;

import io.littlehorse.common.LHConstants;
import io.littlehorse.common.util.LHUtil;
import java.util.Date;

/**
 * The resume point of the timer scan for one partition: every timer maturing strictly before this
 * instant has already been forwarded and deleted. Scans restart from it <i>inclusively</i>, so a
 * timer maturing on exactly this millisecond may still be pending.
 *
 * <p>This is the single piece of state shared between the Kafka Streams thread and the background
 * worker, and the rules around it are what keep timers from being lost:
 *
 * <ul>
 *   <li>It is <b>written only on the Streams thread</b> — once from {@code init()} to seed it from
 *       the persisted hint, and thereafter only by {@link TimerCursorAdvanceAction}. The worker
 *       reads it to decide where to resume, which is why it is {@code volatile}.</li>
 *   <li>It only ever <b>moves forwards</b>. A stale or out-of-order advance is dropped rather than
 *       rewinding the scan.</li>
 *   <li>It must never move past a timer that has not been delivered. Because the background scan
 *       reads a stale IQ view, it can miss a timer written moments earlier; that is why the advance
 *       is verified against the Streams thread's own view before being accepted.</li>
 * </ul>
 *
 * <p>The cursor never runs ahead of wall clock time, which is what lets
 * {@code TimerCoreProcessor.process()} decide whether to store or forward a timer using nothing but
 * {@code System.currentTimeMillis()}: anything it stores matures in the future, and therefore lands
 * above the cursor where a later scan is guaranteed to find it.
 */
final class TimerCursor {

    /**
     * Lower bound used for a partition that has never checkpointed a hint. Sorts before any real
     * timer key, so the first scan sweeps the whole store.
     */
    private static final String BEGINNING_OF_TIME = "000000";

    private volatile long millis;

    /**
     * Only touched by {@link #shouldCheckpoint}, which runs exclusively on the Streams thread.
     */
    private long lastCheckpointedAtMillis;

    long get() {
        return millis;
    }

    /**
     * Seeds the cursor from the persisted hint during {@code init()}.
     */
    void seed(long value) {
        this.millis = value;
    }

    /**
     * @return true if the cursor actually moved.
     */
    boolean advanceTo(long target) {
        if (target <= millis) {
            return false;
        }
        millis = target;
        return true;
    }

    /**
     * Rate-limits persistence of the {@code TimerIteratorHintModel}. Checkpointing on every advance
     * would be a write per punctuation for no benefit: the hint is only ever read at startup, and
     * losing up to a minute of it just means re-scanning a minute's worth of tombstones.
     */
    boolean shouldCheckpoint(long now) {
        if (now - lastCheckpointedAtMillis < LHConstants.TIMER_PUNCTUATOR_HINT_CHECKPOINT_INTERVAL) {
            return false;
        }
        lastCheckpointedAtMillis = now;
        return true;
    }

    /**
     * Converts an instant into the store key prefix that bounds a timer range scan. Timer keys are
     * {@code <lhDbFormat(maturation)>_<guid>}, so this bound is inclusive at the start of a
     * millisecond and exclusive at the end of it.
     */
    static String storeKeyBound(long millis) {
        return millis == 0L ? BEGINNING_OF_TIME : LHUtil.toLhDbFormat(new Date(millis));
    }
}
