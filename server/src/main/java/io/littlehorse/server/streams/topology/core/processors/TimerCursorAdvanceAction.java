package io.littlehorse.server.streams.topology.core.processors;

import io.littlehorse.common.model.LHTimer;
import io.littlehorse.server.streams.store.LHKeyValueIterator;
import io.littlehorse.server.streams.storeinternals.TimerIteratorHintModel;
import io.littlehorse.server.streams.stores.ClusterScopedStore;
import io.littlehorse.server.streams.topology.core.CommandProcessorOutput;
import io.littlehorse.server.streams.topology.core.background.PartitionAction;
import io.littlehorse.server.streams.topology.core.background.PartitionActionApplier;
import java.util.Date;

/**
 * Moves the timer scan's resume point forward, but only as far as the Streams thread can confirm is
 * safe.
 *
 * <p>The background scan proposes a new cursor based on a <b>stale</b> IQv1 view. In the window
 * between that read and this action being applied, {@code process()} may have written a timer that
 * matures inside the range the scan claims to have covered. Accepting the proposal blindly would
 * put that timer permanently behind the cursor, and it would never fire — the failure mode being a
 * workflow that hangs on a sleep or a task timeout forever.
 *
 * <p>So before accepting, we re-scan the claimed range against the Streams thread's own view. Every
 * {@link MatureTimerAction} from the scan that produced this proposal has already been applied
 * (actions are FIFO), so anything still alive in that range is precisely a timer the scan missed,
 * and the cursor is clamped to it instead. The next scan then picks it up.
 */
record TimerCursorAdvanceAction(TimerCursor cursor, long proposedMillis)
        implements PartitionAction<CommandProcessorOutput> {

    @Override
    public void apply(PartitionActionApplier<CommandProcessorOutput> applier) {
        long current = cursor.get();
        if (proposedMillis <= current) {
            return;
        }
        ClusterScopedStore store = applier.coreStore();

        long target = proposedMillis;
        try (LHKeyValueIterator<LHTimer> missed = store.range(
                TimerCursor.storeKeyBound(current), TimerCursor.storeKeyBound(proposedMillis), LHTimer.class)) {
            if (missed.hasNext()) {
                target = Math.min(
                        target, missed.next().getValue().getMaturationTime().getTime());
            }
        }

        if (!cursor.advanceTo(target)) {
            return;
        }

        // Checkpoint the resume point so a restart does not have to iterate from the beginning of
        // the store over every tombstone left behind by delivered timers.
        long now = System.currentTimeMillis();
        if (cursor.shouldCheckpoint(now)) {
            store.put(new TimerIteratorHintModel(new Date(cursor.get())));
        }
    }

    @Override
    public String describe() {
        return "ADVANCE_TIMER_CURSOR to " + proposedMillis;
    }
}
