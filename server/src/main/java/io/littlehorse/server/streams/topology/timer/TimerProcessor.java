package io.littlehorse.server.streams.topology.timer;

import com.google.protobuf.InvalidProtocolBufferException;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.model.LHTimer;
import io.littlehorse.common.proto.Command;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.server.streams.ServerTopology;
import io.littlehorse.server.streams.topology.core.CommandProcessorOutput;
import io.littlehorse.server.streams.util.HeadersUtil;
import java.util.Date;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.processor.Cancellable;
import org.apache.kafka.streams.processor.PunctuationType;
import org.apache.kafka.streams.processor.TaskId;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.KeyValueStore;

@Slf4j
public class TimerProcessor implements Processor<String, CommandProcessorOutput, String, Command> {

    private ProcessorContext<String, Command> context;
    private KeyValueStore<String, LHTimer> timerStore;
    private Cancellable punctuator;
    private String lastSeenKey;
    private final boolean shouldPunctuate;
    private TaskId taskId;

    public TimerProcessor(boolean shouldPunctuate) {
        this.shouldPunctuate = shouldPunctuate;
    }

    @Override
    public void init(final ProcessorContext<String, Command> context) {
        this.context = context;
        taskId = context.taskId();
        timerStore = context.getStateStore(ServerTopology.TIMER_STORE);
        if (shouldPunctuate) {
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

    public void process(final Record<String, CommandProcessorOutput> record) {

        CommandProcessorOutput commandProcessorOutput = record.value();
        if (!record.value().isTimer()) {
            throw new IllegalArgumentException("Output is not a timer record");
        }

        LHTimer timer = new LHTimer((Command) commandProcessorOutput.getCommand(), commandProcessorOutput.getMaturationTime());
        log.trace("Processing timer {}, task {}", timer.partitionKey, context.taskId());


        log.trace("Processing timer {}", timer);

        // If the timer is already matured, no sense in putting it into the store. Just forward now.
        if (timer.maturationTime.getTime() <= System.currentTimeMillis() && shouldPunctuate) {
            sendOneTimer(timer);
        } else {
            timerStore.put(timer.getStoreKey(), timer);
        }
    }

    private void clearTimers(long timestamp) {
        String end = LHUtil.toLhDbFormat(new Date(timestamp));

        try (KeyValueIterator<String, LHTimer> iter = timerStore.range(lastSeenKey, end)) {
            while (iter.hasNext()) {
                KeyValue<String, LHTimer> entry = iter.next();
                sendOneTimer(entry.value);
                timerStore.delete(entry.key);
            }
        }
        lastSeenKey = end;
    }

    private void sendOneTimer(LHTimer timer) {
        Headers metadata = HeadersUtil.metadataHeadersFor(timer.getTenantId(), timer.getPrincipalId());
        try {
            Command cmd = Command.parseFrom(timer.getPayload());
            // Now we gotta forward the timer.
            Record<String, Command> toSend =
                    new Record<>(timer.partitionKey, cmd, timer.maturationTime.getTime(), metadata);
            log.trace("Forwarding timer on task {}. should punctuate: {}. value: {} ", taskId, shouldPunctuate, toSend);
            context.forward(toSend);
        } catch (InvalidProtocolBufferException e) {
            throw new RuntimeException(e);
        }


    }
}
