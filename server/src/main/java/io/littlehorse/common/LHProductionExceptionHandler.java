package io.littlehorse.common;

import java.util.Map;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.errors.TransactionAbortedException;
import org.apache.kafka.streams.errors.ProductionExceptionHandler;

public class LHProductionExceptionHandler implements ProductionExceptionHandler {

    public LHProductionExceptionHandler() {}

    @Override
    public ProductionExceptionHandlerResponse handle(ProducerRecord<byte[], byte[]> record, Exception exception) {
        if (exception instanceof TransactionAbortedException) {
            // It is safe to continue on abortable exceptions caused inside the Kafka Streams TransactionManager
            return ProductionExceptionHandlerResponse.CONTINUE;
        }
        return ProductionExceptionHandlerResponse.FAIL;
    }

    @Override
    public void configure(Map<String, ?> configs) {
        // nothing to do
    }
}
