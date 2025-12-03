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

public class ProcessorOutputRouter implements Processor<String, CommandProcessorOutput, String, Forwardable> {

    private ProcessorContext<String, Forwardable> context;

    @Override
    public void init(ProcessorContext<String, Forwardable> context) {
        this.context = context;
    }

    @Override
    public void process(Record<String, CommandProcessorOutput> record) {
        CommandProcessorOutput processorOutput = record.value();
        LHSerializable<?> payload = processorOutput.getPayload();
        if (payload instanceof LHTimer) {
            Record<String, LHTimer> timerRecord = record.withValue((LHTimer) payload);
            this.context.forward(timerRecord, ServerTopologyV2.TIMER_PROCESSOR_NAME);
        } else if (payload instanceof OutputTopicRecordModel) {
            this.context.forward(record, ServerTopologyV2.OUTPUT_TOPIC_PROCESSOR_NAME);
        } else {
            throw new IllegalArgumentException("Unknown payload type: " + payload.getClass());
        }
    }
}
