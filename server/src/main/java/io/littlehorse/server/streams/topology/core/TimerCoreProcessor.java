package io.littlehorse.server.streams.topology.core;

import io.littlehorse.common.LHConstants;
import io.littlehorse.common.model.LHTimer;
import io.littlehorse.common.proto.StoreableType;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.server.streams.ServerTopologyV2;
import io.littlehorse.server.streams.store.LHIterKeyValue;
import io.littlehorse.server.streams.store.LHKeyValueIterator;
import io.littlehorse.server.streams.storeinternals.TimerIteratorHintModel;
import io.littlehorse.server.streams.stores.ClusterScopedStore;
import io.littlehorse.server.streams.util.HeadersUtil;
import java.util.Date;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.processor.Cancellable;
import org.apache.kafka.streams.processor.PunctuationType;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.state.KeyValueStore;

@Slf4j
public class TimerCoreProcessor implements Processor<String, LHTimer, String, Object> {

    private ProcessorContext<String, Object> context;
    private ClusterScopedStore lhKeyValueStore;
    private Cancellable punctuator;
    private long lastSeenTimestampMillis;
    private long lastCheckpointedHintTimeMillis;

    private boolean seenLastKey = false;

    private final boolean forwardTimers;

    public TimerCoreProcessor(boolean forwardTimers) {
        this.forwardTimers = forwardTimers;
    }

    @Override
    public void init(final ProcessorContext<String, Object> context) {
        this.context = context;
        KeyValueStore<String, Bytes> nativeRocksDBStore = context.getStateStore(ServerTopologyV2.CORE_STORE_NAME);
        this.lhKeyValueStore = ClusterScopedStore.newInstance(nativeRocksDBStore, null);
        if (forwardTimers) {
            this.punctuator = context.schedule(
                    LHConstants.TIMER_PUNCTUATOR_INTERVAL, PunctuationType.WALL_CLOCK_TIME, this::clearTimers);

            TimerIteratorHintModel timerHint =
                    lhKeyValueStore.get(TimerIteratorHintModel.TIMER_ITERATOR_HINT_KEY, TimerIteratorHintModel.class);
            if (timerHint != null) {
                this.lastSeenTimestampMillis =
                        LHLibUtil.fromProtoTs(timerHint.getLastProcessedTimer()).getTime();
            } else {
                this.lastSeenTimestampMillis = 0L;
            }
        }
    }

    @Override
    public void close() {
        if (punctuator != null) {
            punctuator.cancel();
        }
    }

    @Override
    public void process(final Record<String, LHTimer> record) {
        LHTimer timer = record.value();
        boolean isMatured = timer.maturationTime.getTime() <= lastSeenTimestampMillis;
        if (!forwardTimers || !isMatured) {
            storeOneTimer(timer);
            return;
        }
        sendOneTimer(timer);
    }

    private void clearTimers(long timestamp) {
        String end = LHUtil.toLhDbFormat(new Date(timestamp));
        String lastSeenKey =
                lastSeenTimestampMillis == 0L ? "000000" : LHUtil.toLhDbFormat(new Date(lastSeenTimestampMillis));

        if (!seenLastKey) {
            System.out.println(lastSeenKey);
            seenLastKey = true;
        }

        try (LHKeyValueIterator<LHTimer> iter = lhKeyValueStore.range(lastSeenKey, end, LHTimer.class)) {
            long startTimeMs = System.currentTimeMillis();

            while (iter.hasNext()) {
                LHIterKeyValue<LHTimer> entry = iter.next();
                LHTimer timer = entry.getValue();
                sendOneTimer(timer);
                lhKeyValueStore.delete(entry.getKey(), StoreableType.LH_TIMER);
                lastSeenTimestampMillis = timer.getMaturationTime().getTime();

                if (System.currentTimeMillis() - startTimeMs > LHConstants.MAX_MS_PER_TIMER_PUNCTUATION) {
                    // TODO: add a prometheus metric to track the lag for each partition of how far we have
                    // left to iterate.
                    break;
                }
            }
        }

        // Maybe store a hint. We only need to do this once every minute or so.
        // To understand why we do this, look at this RocksDB documentation:
        // https://github.com/facebook/rocksdb/wiki/Implement-Queue-Service-Using-RocksDB
        if (System.currentTimeMillis() - lastCheckpointedHintTimeMillis
                > LHConstants.TIMER_PUNCTUATOR_HINT_CHECKPOINT_INTERVAL) {
            lastCheckpointedHintTimeMillis = System.currentTimeMillis();
            lhKeyValueStore.put(new TimerIteratorHintModel(new Date(lastSeenTimestampMillis)));
        }
    }

    private void sendOneTimer(LHTimer timer) {
        Headers metadata = HeadersUtil.metadataHeadersFor(timer.getTenantId(), timer.getPrincipalId());
        // Now we gotta forward the timer.
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
