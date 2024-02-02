package io.littlehorse.canary.aggregator;

import java.util.Map;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.streams.errors.DeserializationExceptionHandler;
import org.apache.kafka.streams.processor.ProcessorContext;

public class MetricDeserializationExceptionHandler implements DeserializationExceptionHandler {
    @Override
    public DeserializationHandlerResponse handle(
            final ProcessorContext context, final ConsumerRecord<byte[], byte[]> record, final Exception exception) {
        return null;
    }

    @Override
    public void configure(final Map<String, ?> configs) {}
}
