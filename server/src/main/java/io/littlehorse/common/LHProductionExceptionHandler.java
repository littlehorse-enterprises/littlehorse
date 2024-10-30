package io.littlehorse.common;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.errors.TransactionAbortedException;
import org.apache.kafka.streams.errors.ProductionExceptionHandler;

import java.util.Map;

public class LHProductionExceptionHandler implements ProductionExceptionHandler {

    public LHProductionExceptionHandler() {

    }

    @Override
    public ProductionExceptionHandlerResponse handle(ProducerRecord<byte[], byte[]> record, Exception exception) {
        if (exception instanceof TransactionAbortedException) {
            return ProductionExceptionHandlerResponse.CONTINUE;
        }
        return ProductionExceptionHandlerResponse.FAIL;
    }

    @Override
    public void configure(Map<String, ?> configs) {
        //nothing to do
    }
}
