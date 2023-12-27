package io.littlehorse.common.model.repartitioncommand;

import io.littlehorse.server.streams.stores.TenantScopedStore;
import org.apache.kafka.streams.processor.api.ProcessorContext;

public interface RepartitionSubCommand {
    public void process(TenantScopedStore repartitionedStore, ProcessorContext<Void, Void> ctx);

    public String getPartitionKey();
}
