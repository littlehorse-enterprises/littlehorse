package io.littlehorse.server.streamsimpl.coreprocessors;

import io.littlehorse.common.model.observabilityevent.ObservabilityEvent;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;

public class MetricsLocalProcessor
    implements Processor<String, ObservabilityEvent, String, MetricsProcessorOutput> {

    public void init(ProcessorContext<String, MetricsProcessorOutput> ctx) {
        // TODO
    }

    public void process(final Record<String, ObservabilityEvent> record) {
        // TODO
    }
}
