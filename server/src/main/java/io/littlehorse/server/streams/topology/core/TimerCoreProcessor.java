package io.littlehorse.server.streams.topology.timer;

import io.littlehorse.common.LHConstants;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.LHTimer;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.server.streams.ServerTopologyV2;
import io.littlehorse.server.streams.util.HeadersUtil;
import java.util.Date;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.processor.Cancellable;
import org.apache.kafka.streams.processor.PunctuationType;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.KeyValueStore;

@Slf4j
public class TimerCoreProcessor implements Processor<String, LHTimer, String, LHTimer> {

    private ProcessorContext<String, LHTimer> context;
    private KeyValueStore<String, Bytes> timerStore;
    private Cancellable punctuator;
    private String lastSeenKey;

    private final boolean forwardTimers;

    public TimerCoreProcessor(boolean forwardTimers) {
        this.forwardTimers = forwardTimers;
    }

    public void init(final ProcessorContext<String, LHTimer> context) {
        this.context = context;
        timerStore = context.getStateStore(ServerTopologyV2.CORE_STORE_NAME);
        if (forwardTimers) {
            this.punctuator = context.schedule(
                    LHConstants.TIMER_PUNCTUATOR_INTERVAL, PunctuationType.WALL_CLOCK_TIME, this::clearTimers);

            this.lastSeenKey = "0000000000";
        }
    }

    @Override
    public void close() {
        if (punctuator != null) {
            punctuator.cancel();
        }
    }

    public void process(final Record<String, LHTimer> record) {
        LHTimer timer = record.value();
        if (!forwardTimers) {
            storeOneTimer(timer);
            return;
        }
        if (timer.isRepartition()) {
            context.forward(record);
            return;
        }

        // If the timer is already matured, no sense in putting it into the store. Just forward now.
        if (timer.maturationTime.getTime() <= System.currentTimeMillis()) {
            sendOneTimer(timer);
        } else {
            storeOneTimer(timer);
        }
    }

    private void clearTimers(long timestamp) {
        String end = LHUtil.toLhDbFormat(new Date(timestamp));

        try (KeyValueIterator<String, Bytes> iter = timerStore.range(lastSeenKey, end)) {
            while (iter.hasNext()) {
                KeyValue<String, Bytes> entry = iter.next();
                sendOneTimer(LHSerializable.fromBytes(entry.value.get(), LHTimer.class, null));
                timerStore.delete(entry.key);
            }
        }
        lastSeenKey = end;
    }

    private void sendOneTimer(LHTimer timer) {
        Headers metadata = HeadersUtil.metadataHeadersFor(timer.getTenantId(), timer.getPrincipalId());
        // Now we gotta forward the timer.
        Record<String, LHTimer> toSend =
                new Record<String, LHTimer>(timer.partitionKey, timer, timer.maturationTime.getTime(), metadata);
        context.forward(toSend);
    }

    protected void storeOneTimer(LHTimer timer) {
        timerStore.put(timer.getStoreKey(), new Bytes(timer.toBytes()));
    }
}
