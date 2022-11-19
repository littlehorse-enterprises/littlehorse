package io.littlehorse.server.streamsbackend.coreserver;

import io.littlehorse.common.LHConfig;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.model.command.Command;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;

public class CoreServerProcessor
    implements Processor<String, Command, String, CoreServerProcessorOutput> {

    private ProcessorContext<String, CoreServerProcessorOutput> ctx;
    private CoreServerProcessorDaoImpl backend;
    private LHConfig config;

    public CoreServerProcessor(LHConfig config) {
        this.config = config;
    }

    @Override
    public void init(final ProcessorContext<String, CoreServerProcessorOutput> ctx) {
        this.ctx = ctx;
        backend = new CoreServerProcessorDaoImpl(this.ctx, config);
    }

    @Override
    public void process(final Record<String, Command> commandRecord) {
        try {
            Command command = commandRecord.value();
            LHSerializable<?> response = command.process(backend);
            if (command.hasResponse()) {
                // TOOD: save the response
                backend.saveResponse(response, command);
            }
            backend.commitChanges();
        } catch (Exception exn) {
            exn.printStackTrace();
            backend.abortChanges();
            // Should we have a DLQ? I don't think that makes sense...the internals
            // of a database like Postgres don't have a DQL for their WAL.
        }
    }
}
