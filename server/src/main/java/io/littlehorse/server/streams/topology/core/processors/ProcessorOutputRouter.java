package io.littlehorse.server.streams.topology.core.processors;

import java.util.function.BiConsumer;

import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;

import com.google.protobuf.InvalidProtocolBufferException;

import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.LHTimer;
import io.littlehorse.common.model.outputtopic.OutputTopicRecordModel;
import io.littlehorse.common.model.repartitioncommand.RepartitionCommand;
import io.littlehorse.common.proto.Command;
import io.littlehorse.server.streams.ServerTopologyV2;
import io.littlehorse.server.streams.topology.core.CommandProcessorOutput;
import io.littlehorse.server.streams.topology.core.Forwardable;

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
            createCommandProcessorRouter(String timerProcessorName, String outputTopicProcessorName) {
        return new ProcessorOutputRouter<>((c, s) -> ProcessorOutputRouter.processCommandProcessorOutput(
                c, s, timerProcessorName, outputTopicProcessorName));
    }

    public static ProcessorOutputRouter<String, Forwardable, String, Forwardable> createPassthroughRepartitionRouter() {
        return new ProcessorOutputRouter<>(ProcessorOutputRouter::processPassThrough);
    }

    public static ProcessorOutputRouter<String, LHTimer, String, Object> createTimerProcessorRouter() {
        return new ProcessorOutputRouter<>(ProcessorOutputRouter::processTimerProcessorOutput);
    }

    private static void processCommandProcessorOutput(
            ProcessorContext<String, Forwardable> context,
            Record<String, CommandProcessorOutput> record,
            String timerProcessorName,
            String outputTopicProcessorName) {
        CommandProcessorOutput processorOutput = record.value();
        LHSerializable<?> payload = processorOutput.getPayload();
        if (payload instanceof LHTimer) {
            Record<String, LHTimer> timerRecord = record.withValue((LHTimer) payload);
            context.forward(timerRecord, timerProcessorName);
        } else if (payload instanceof OutputTopicRecordModel) {
            context.forward(record, outputTopicProcessorName);
        } else if (payload instanceof RepartitionCommand) {
            RepartitionCommand repartitionCommand = (RepartitionCommand) payload;
            String partitionKey = repartitionCommand.getSubCommand().getPartitionKey();
            context.forward(record.withKey(partitionKey), ServerTopologyV2.REPARTITION_PASSTHROUGH_PROCESSOR);
        } else {
            throw new IllegalArgumentException("Unknown payload type: " + payload.getClass());
        }
    }

    private static void processTimerProcessorOutput(
            ProcessorContext<String, Object> context, Record<String, LHTimer> record) {
        try {
            LHTimer timer = record.value();
            Record<String, Command> nextRecord = record.withValue(Command.parseFrom(timer.getPayload()));
            if (timer.isRepartition()) {
                context.forward(
                        nextRecord.withKey(timer.getPartitionKey()),
                        ServerTopologyV2.REPARTITION_PASSTHROUGH_PROCESSOR);
            } else {
                context.forward(nextRecord, ServerTopologyV2.TIMER_COMMAND_PROCESSOR_NAME);
            }
        } catch (InvalidProtocolBufferException e) {
            throw new RuntimeException(e);
        }
    }

    private static void processPassThrough(
            ProcessorContext<String, Forwardable> context, Record<String, Forwardable> record) {
        context.forward(record);
    }
}
