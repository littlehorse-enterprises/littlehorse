package io.littlehorse.server.streams.topology.core.processors;

import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.LHTimer;
import io.littlehorse.common.model.outputtopic.OutputTopicRecordModel;
import io.littlehorse.server.streams.ServerTopologyV2;
import io.littlehorse.server.streams.topology.core.CommandProcessorOutput;
import io.littlehorse.server.streams.topology.core.Forwardable;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;

import java.util.function.BiConsumer;

public class ProcessorOutputRouter<KIn, VIn, KOut, VOut> implements Processor<String, CommandProcessorOutput, String, Forwardable> {

    private ProcessorContext<String, Forwardable> context;
    private BiConsumer<ProcessorContext<String, Forwardable>, Record<String, CommandProcessorOutput>> processor;

    private ProcessorOutputRouter(BiConsumer<ProcessorContext<String, Forwardable>, Record<String, CommandProcessorOutput>> processor) {
        this.processor = processor;
    }

    @Override
    public void init(ProcessorContext<String, Forwardable> context) {
        this.context = context;
    }

    @Override
    public void process(Record<String, CommandProcessorOutput> record) {
        processor.accept(context, record);
    }

    public static ProcessorOutputRouter<String, CommandProcessorOutput, String, Forwardable> createCommandProcessorRouter() {
        return new ProcessorOutputRouter<>(ProcessorOutputRouter::processCommandProcessorOutput);
    }

    public static ProcessorOutputRouter<String, Forwardable, String, Forwardable> createPassthroughRepartitionRouter() {
        return new ProcessorOutputRouter<>(ProcessorOutputRouter::processPassThrough);
    }

    public static ProcessorOutputRouter createTimerProcessorRouter() {

    }

    private static void processCommandProcessorOutput(ProcessorContext<String, Forwardable> context, Record<String, CommandProcessorOutput> record) {
        CommandProcessorOutput processorOutput = record.value();
        LHSerializable<?> payload = processorOutput.getPayload();
        if (payload instanceof LHTimer) {
            Record<String, LHTimer> timerRecord = record.withValue((LHTimer) payload);
            context.forward(timerRecord, ServerTopologyV2.TIMER_PROCESSOR_NAME);
        } else if (payload instanceof OutputTopicRecordModel) {
            context.forward(record, ServerTopologyV2.OUTPUT_TOPIC_PROCESSOR_NAME);
        } else {
            throw new IllegalArgumentException("Unknown payload type: " + payload.getClass());
        }
    }

    private static void processTimerProcessorOutput(ProcessorContext<String, Forwardable> context, Record<String, CommandProcessorOutput> record) {
        LHTimer timer = (LHTimer) record.value().payload;
        if (timer.isRepartition()) {
            context.forward(record);
        }
    }

    private static void processPassThrough(ProcessorContext<String, Forwardable> context, Record<String, Forwardable> record) {
        context.forward(record);
    }
}
