package io.littlehorse.common.model.repartitioncommand;

import io.littlehorse.server.streams.store.ModelStore;
import org.apache.kafka.streams.processor.api.ProcessorContext;

public interface RepartitionSubCommand {
    public void process(ModelStore repartitionedStore, ProcessorContext<Void, Void> ctx);

    public String getPartitionKey();
}
