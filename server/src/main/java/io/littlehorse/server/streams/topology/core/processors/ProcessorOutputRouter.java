package io.littlehorse.server.streams.topology.core.processors;

import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.LHTimer;
import io.littlehorse.common.model.outputtopic.OutputTopicRecordModel;
import io.littlehorse.server.streams.ServerTopologyV2;
import io.littlehorse.server.streams.topology.core.CommandProcessorOutput;
import io.littlehorse.server.streams.topology.core.Forwardable;
import java.util.function.BiConsumer;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;

public class ProcessorOutputRouter<KIn, VIn, KOut, VOut> implements Processor<KIn, VIn, KOut, VOut> {

    private ProcessorContext<KOut, VOut> context;
    private final BiConsumer<ProcessorContext<KOut, VOut>, Record<KIn, VIn>> processor;

    private ProcessorOutputRouter(BiConsumer<ProcessorContext<KOut, VOut>, Record<KIn, VIn>> processor) {
        this.processor = processor;
    }

    @Override
    public void init(ProcessorContext<KOut, VOut> context) {
        this.context = context;
    }

    @Override
    public void process(Record<KIn, VIn> record) {
        processor.accept(context, record);
    }

    public static ProcessorOutputRouter<String, CommandProcessorOutput, String, Forwardable>
            createCommandProcessorRouter() {
        return new ProcessorOutputRouter<>(ProcessorOutputRouter::processCommandProcessorOutput);
    }

    public static ProcessorOutputRouter<String, Forwardable, String, Forwardable> createPassthroughRepartitionRouter() {
        return new ProcessorOutputRouter<>(ProcessorOutputRouter::processPassThrough);
    }

    public static ProcessorOutputRouter<String, LHTimer, String, LHSerializable<?>> createTimerProcessorRouter() {
        return new ProcessorOutputRouter<>(ProcessorOutputRouter::processTimerProcessorOutput);
    }

    private static void processCommandProcessorOutput(
            ProcessorContext<String, Forwardable> context, Record<String, CommandProcessorOutput> record) {
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

    private static void processTimerProcessorOutput(
            ProcessorContext<String, LHSerializable<?>> context, Record<String, LHTimer> record) {
        LHTimer timer = record.value();
        if (timer.isRepartition()) {
            context.forward(record, ServerTopologyV2.REPARTITION_PASSTHROUGH_PROCESSOR);
        } else {
            context.forward(record, ServerTopologyV2.TIMER_COMMAND_PROCESSOR_NAME);
        }
    }

    private static void processPassThrough(
            ProcessorContext<String, Forwardable> context, Record<String, Forwardable> record) {
        context.forward(record);
    }
}
