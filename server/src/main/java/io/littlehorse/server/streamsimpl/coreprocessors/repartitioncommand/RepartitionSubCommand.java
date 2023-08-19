package io.littlehorse.server.streamsimpl.coreprocessors.repartitioncommand;

import io.littlehorse.server.streamsimpl.storeinternals.LHStoreWrapper;
import org.apache.kafka.streams.processor.api.ProcessorContext;

public interface RepartitionSubCommand {
    public void process(LHStoreWrapper repartitionedStore, ProcessorContext<Void, Void> ctx);

    public String getPartitionKey();
}
