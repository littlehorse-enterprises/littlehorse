package io.littlehorse.server.streamsimpl.coreprocessors;

import io.littlehorse.server.streamsimpl.coreprocessors.repartitioncommand.RepartitionCommand;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;

public class RepartitionCommandProcessor
    implements Processor<String, RepartitionCommand, Void, Void> {

    public void init(final ProcessorContext<Void, Void> ctx) {}

    public void process(final Record<String, RepartitionCommand> record) {}
}
