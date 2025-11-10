package io.littlehorse.server.streams.topology.core;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.errors.RecordTooLargeException;
import org.apache.kafka.streams.errors.DefaultProductionExceptionHandler;
import org.apache.kafka.streams.errors.ErrorHandlerContext;

/**
 * Custom implementation of Kafka's DefaultProductionExceptionHandler that provides
 * specific handling for RecordTooLargeException.
 * <p>
 * This handler extends the default Kafka Streams production exception handling
 * by gracefully handling cases where records exceed Kafka's maximum message size limit.
 * In such cases, it logs the command and continues processing instead of failing.
 */
@Slf4j
public class LHProductionExceptionHandler extends DefaultProductionExceptionHandler {

    /**
     * Handles exceptions that occur during record production in Kafka Streams.
     */
    @Override
    public ProductionExceptionHandlerResponse handle(
            ErrorHandlerContext context, ProducerRecord<byte[], byte[]> record, Exception exception) {
        if (exception instanceof RecordTooLargeException) {
            log.debug("Dropping command due to record size exceeding Kafka's maximum message size limit: ", exception);
            return ProductionExceptionHandlerResponse.CONTINUE;
        }
        return super.handle(context, record, exception);
    }
}
