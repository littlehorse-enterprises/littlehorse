package io.littlehorse.server.streams.topology.core;

import io.littlehorse.common.model.LHTimer;
import io.littlehorse.server.streams.ServerTopologyV2;
import io.littlehorse.server.streams.stores.ClusterScopedStore;
import io.littlehorse.server.streams.util.HeadersUtil;
import java.util.Date;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.state.KeyValueStore;

/**
 * Persists timers, and passes already-matured ones straight through to the timer command path.
 *
 * <p>This processor has no punctuator. Finding matured timers is done by {@code TimerScanJob}, on
 * the single background worker owned by {@code CommandProcessor}; the deliveries it produces are
 * applied from that processor's punctuator and re-enter here through the ordinary command-output
 * router, which is why {@code process()} has to handle an already-matured timer at all.
 *
 * <p>The store/forward decision is made against wall clock time. That is safe because the scan's
 * resume cursor never runs ahead of the wall clock: a timer maturing in the future is therefore
 * always stored above the cursor, where a later scan is guaranteed to find it.
 */
@Slf4j
public class TimerCoreProcessor implements Processor<String, LHTimer, String, Object> {

    private ProcessorContext<String, Object> context;
    private ClusterScopedStore lhKeyValueStore;

    private final boolean forwardTimers;

    public TimerCoreProcessor(boolean forwardTimers) {
        this.forwardTimers = forwardTimers;
    }

    @Override
    public void init(final ProcessorContext<String, Object> context) {
        this.context = context;
        KeyValueStore<String, Bytes> nativeStore = context.getStateStore(ServerTopologyV2.CORE_STORE_NAME);
        this.lhKeyValueStore = ClusterScopedStore.newInstance(nativeStore, null);
    }

    @Override
    public void process(final Record<String, LHTimer> record) {
        LHTimer timer = record.value();
        boolean isMatured = timer.getMaturationTime().getTime() <= System.currentTimeMillis();
        if (!forwardTimers || !isMatured) {
            storeOneTimer(timer);
            return;
        }
        // Storing a matured timer would put it at or below the scan cursor, where nothing would
        // pick it up again. Pass it straight through instead.
        sendOneTimer(timer);
    }

    private void sendOneTimer(LHTimer timer) {
        Headers metadata = HeadersUtil.metadataHeadersFor(timer.getTenantId(), timer.getPrincipalId());
        Record<String, LHTimer> toSend =
                new Record<>(timer.partitionKey, timer, timer.maturationTime.getTime(), metadata);
        context.forward(toSend);
    }

    protected void storeOneTimer(LHTimer timer) {
        Date currentDate = new Date();
        if (!forwardTimers && timer.getMaturationTime().compareTo(currentDate) < 0) {
            // Resetting the maturation time to the current time if the time is in the past.
            timer.setMaturationTime(currentDate);
        }
        lhKeyValueStore.put(timer);
    }
}
