package io.littlehorse.server.streams.topology.core.processors;

import io.littlehorse.server.streams.topology.core.CommandProcessorOutput;
import org.apache.kafka.streams.processor.api.ContextualProcessor;
import org.apache.kafka.streams.processor.api.Record;

public class PassThroughProcessor extends ContextualProcessor<String, CommandProcessorOutput, String, CommandProcessorOutput> {

    private final String child;

    public PassThroughProcessor(String child) {
        this.child = child;
    }

    @Override
    public void process(Record<String, CommandProcessorOutput> record) {
        context().forward(record, child);
    }
}
