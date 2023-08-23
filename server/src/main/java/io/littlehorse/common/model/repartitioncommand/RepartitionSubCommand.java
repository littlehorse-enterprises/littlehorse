package io.littlehorse.common.model.repartitioncommand;

import io.littlehorse.server.streams.store.RocksDBWrapper;
import org.apache.kafka.streams.processor.api.ProcessorContext;

public interface RepartitionSubCommand {
    public void process(RocksDBWrapper repartitionedStore, ProcessorContext<Void, Void> ctx);

    public String getPartitionKey();
}
