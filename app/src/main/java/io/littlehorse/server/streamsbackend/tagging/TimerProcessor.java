package io.littlehorse.server.streamsbackend.tagging;

import io.littlehorse.common.LHConstants;
import io.littlehorse.common.model.wfrun.LHTimer;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.server.streamsbackend.ServerTopology;
import java.util.Date;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.processor.Cancellable;
import org.apache.kafka.streams.processor.PunctuationType;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.KeyValueStore;

public class TimerProcessor implements Processor<String, LHTimer, String, LHTimer> {

    private ProcessorContext<String, LHTimer> context;
    private KeyValueStore<String, LHTimer> timerStore;
    private Cancellable punctuator;

    public void init(final ProcessorContext<String, LHTimer> context) {
        this.context = context;
        timerStore = context.getStateStore(ServerTopology.TIMER_STORE);
        this.punctuator =
            context.schedule(
                LHConstants.PUNCTUATOR_INERVAL,
                PunctuationType.WALL_CLOCK_TIME,
                this::clearTimers
            );
    }

    @Override
    public void close() {
        punctuator.cancel();
    }

    public void process(final Record<String, LHTimer> record) {
        LHTimer timer = record.value();
        timerStore.put(timer.getStoreKey(), timer);
    }

    private void clearTimers(long timestamp) {
        String start = "00000000";
        String end = LHUtil.toLhDbFormat(new Date(timestamp));

        try (KeyValueIterator<String, LHTimer> iter = timerStore.range(start, end)) {
            while (iter.hasNext()) {
                KeyValue<String, LHTimer> entry = iter.next();
                LHTimer timer = entry.value;
                if (
                    !entry.key.equals(timer.getStoreKey())
                ) throw new RuntimeException("WTF?");

                // Now we gotta forward the timer.
                Record<String, LHTimer> record = new Record<String, LHTimer>(
                    timer.key,
                    timer,
                    timer.maturationTime.getTime()
                );
                context.forward(record);
                timerStore.delete(entry.key);
            }
        }
    }
}
