package io.littlehorse.server.streams.topology.core.processors;

import io.littlehorse.common.model.LHTimer;
import io.littlehorse.server.streams.stores.ClusterScopedStore;
import io.littlehorse.server.streams.topology.core.CommandProcessorOutput;
import io.littlehorse.server.streams.topology.core.background.PartitionAction;
import io.littlehorse.server.streams.topology.core.background.PartitionActionApplier;
import io.littlehorse.server.streams.util.HeadersUtil;
import org.apache.kafka.streams.processor.api.Record;

/**
 * Delivers one matured timer: removes it from the store and forwards it downstream, in a single
 * Kafka Streams transaction.
 *
 * <p><b>The store entry is the delivery token.</b> The forward happens only if the timer is still
 * present, and the delete that consumes it commits atomically with it. That is what makes the
 * background scan safe despite reading a stale IQv1 view: the scan can re-discover a timer it
 * already delivered on a previous tick (the delete is committed but not yet visible to IQ), and
 * this check drops the duplicate instead of firing the command twice. Without it, a re-scan would
 * mean a workflow task running more than once.
 *
 * <p>This is applied from {@code CommandProcessor}'s punctuator, so the forward leaves from that
 * node and takes the ordinary command-output path: the router recognises an {@link LHTimer} payload
 * and hands it to {@code TimerCoreProcessor}, which sees an already-matured timer and passes it
 * straight through to the timer command path. Emitting it as a {@link CommandProcessorOutput} — the
 * exact shape {@code LHTaskManager} uses to schedule a timer in the first place — is what lets a
 * single background thread serve the whole task without needing the timer node's own context.
 */
record MatureTimerAction(LHTimer timer, String timerTopic) implements PartitionAction<CommandProcessorOutput> {

    @Override
    public void apply(PartitionActionApplier<CommandProcessorOutput> applier) {
        ClusterScopedStore store = applier.coreStore();
        if (store.get(timer.getStoreKey(), LHTimer.class) == null) {
            return;
        }
        store.delete(timer);
        applier.forward(new Record<>(
                timer.getPartitionKey(),
                new CommandProcessorOutput(timerTopic, timer, timer.getPartitionKey()),
                timer.getMaturationTime().getTime(),
                HeadersUtil.metadataHeadersFor(timer.getTenantId(), timer.getPrincipalId())));
    }

    @Override
    public String describe() {
        return "MATURE_TIMER " + timer.getStoreKey();
    }
}
