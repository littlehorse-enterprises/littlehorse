package io.littlehorse.canary.aggregator;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.streams.errors.DeserializationExceptionHandler;
import org.apache.kafka.streams.processor.ProcessorContext;

@Slf4j
public class MetricDeserializationExceptionHandler implements DeserializationExceptionHandler {

    private AtomicInteger errorsCounter;

    @Override
    public DeserializationHandlerResponse handle(
            final ProcessorContext context, final ConsumerRecord<byte[], byte[]> record, final Exception exception) {
        log.warn("Exception caught {}", exception.getMessage());
        return DeserializationHandlerResponse.CONTINUE;
    }

    @Override
    public void configure(final Map<String, ?> configs) {}
}
