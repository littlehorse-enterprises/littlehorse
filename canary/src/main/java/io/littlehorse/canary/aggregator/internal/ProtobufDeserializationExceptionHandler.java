package io.littlehorse.canary.aggregator.internal;

import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.streams.errors.DeserializationExceptionHandler;
import org.apache.kafka.streams.processor.ProcessorContext;

@Slf4j
public class ProtobufDeserializationExceptionHandler implements DeserializationExceptionHandler {

    @Override
    public DeserializationHandlerResponse handle(
            final ProcessorContext context, final ConsumerRecord<byte[], byte[]> record, final Exception exception) {

        if (exception instanceof ProtobufDeserializationException) {
            return DeserializationHandlerResponse.CONTINUE;
        }

        log.error("Unexpected error when deserializing", exception);
        return DeserializationHandlerResponse.FAIL;
    }

    @Override
    public void configure(final Map<String, ?> configs) {}
}
