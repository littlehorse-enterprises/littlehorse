package io.littlehorse.server.streamsimpl.coreprocessors;

import io.littlehorse.common.LHConfig;
import io.littlehorse.server.streamsimpl.ServerTopology;
import io.littlehorse.server.streamsimpl.coreprocessors.repartitioncommand.RepartitionCommand;
import io.littlehorse.server.streamsimpl.storeinternals.LHStoreWrapper;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.log4j.Logger;

public class RepartitionCommandProcessor
    implements Processor<String, RepartitionCommand, Void, Void> {

    private static final Logger log = Logger.getLogger(
        RepartitionCommandProcessor.class
    );

    private LHStoreWrapper store;
    private LHConfig config;
    private ProcessorContext<Void, Void> ctx;

    public RepartitionCommandProcessor(LHConfig config) {
        this.config = config;
    }

    public void init(final ProcessorContext<Void, Void> ctx) {
        this.ctx = ctx;
        store =
            new LHStoreWrapper(
                ctx.getStateStore(ServerTopology.CORE_REPARTITION_STORE),
                config
            );
    }

    public void process(final Record<String, RepartitionCommand> record) {
        if (record.value() != null) {
            log.debug("Received a metric update!");
            record.value().process(store, ctx);
        }
    }
}
