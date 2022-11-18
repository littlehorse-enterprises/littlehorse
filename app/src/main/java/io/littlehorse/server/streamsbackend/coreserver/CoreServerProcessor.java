package io.littlehorse.server.streamsbackend.coreserver;

import io.littlehorse.common.model.command.Command;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;

public class CoreServerProcessor
    implements Processor<String, Command, String, CoreServerProcessorOutput> {

    private ProcessorContext<String, CoreServerProcessorOutput> ctx;

    @Override
    public void init(final ProcessorContext<String, CoreServerProcessorOutput> ctx) {
        this.ctx = ctx;
    }

    @Override
    public void process(final Record<String, Command> command) {}
}
