package io.littlehorse.server.streamsbackend.coreserver;

import io.littlehorse.common.LHConfig;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.model.command.Command;
import java.time.Duration;
import org.apache.kafka.streams.processor.PunctuationType;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;

public class CommandProcessor
    implements Processor<String, Command, String, CommandProcessorOutput> {

    private ProcessorContext<String, CommandProcessorOutput> ctx;
    private CommandProcessorDaoImpl dao;
    private LHConfig config;

    public CommandProcessor(LHConfig config) {
        this.config = config;
    }

    @Override
    public void init(final ProcessorContext<String, CommandProcessorOutput> ctx) {
        this.ctx = ctx;
        dao = new CommandProcessorDaoImpl(this.ctx, config);
        ctx.schedule(
            Duration.ofMillis(100),
            PunctuationType.WALL_CLOCK_TIME,
            dao::broadcastChanges
        );
    }

    @Override
    public void process(final Record<String, Command> commandRecord) {
        try {
            Command command = commandRecord.value();
            System.out.println(command.toJson());
            dao.setCommand(command);
            LHSerializable<?> response = command.process(dao, config);
            if (command.hasResponse()) {
                dao.saveResponse(response, command);
            }
            dao.commitChanges();
        } catch (Exception exn) {
            exn.printStackTrace();
            dao.abortChanges();
            // Should we have a DLQ? I don't think that makes sense...the internals
            // of a database like Postgres don't have a DQL for their WAL.
        }
    }
}
