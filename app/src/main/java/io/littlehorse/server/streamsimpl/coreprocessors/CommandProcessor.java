package io.littlehorse.server.streamsimpl.coreprocessors;

import com.google.protobuf.ByteString;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.model.command.AbstractResponse;
import io.littlehorse.common.model.command.Command;
import io.littlehorse.common.proto.CommandResultPb;
import io.littlehorse.common.proto.StoreQueryStatusPb;
import io.littlehorse.common.proto.WaitForCommandReplyPb;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.server.KafkaStreamsServerImpl;
import java.util.Date;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;

public class CommandProcessor
    implements Processor<String, Command, String, CommandProcessorOutput> {

    private ProcessorContext<String, CommandProcessorOutput> ctx;
    private KafkaStreamsLHDAOImpl dao;
    private LHConfig config;
    private KafkaStreamsServerImpl server;

    public CommandProcessor(LHConfig config, KafkaStreamsServerImpl server) {
        this.config = config;
        this.server = server;
    }

    @Override
    public void init(final ProcessorContext<String, CommandProcessorOutput> ctx) {
        // temporary hack

        this.ctx = ctx;
        dao = new KafkaStreamsLHDAOImpl(this.ctx, config, server);
        dao.onPartitionClaimed();
    }

    @Override
    public void process(final Record<String, Command> commandRecord) {
        Command command = commandRecord.value();
        dao.setCommand(command);
        try {
            AbstractResponse<?> response = command.process(dao, config);
            dao.commitChanges();
            if (command.hasResponse() && command.commandId != null) {
                WaitForCommandReplyPb cmdReply = WaitForCommandReplyPb
                    .newBuilder()
                    .setCode(StoreQueryStatusPb.RSQ_OK)
                    .setResult(
                        CommandResultPb
                            .newBuilder()
                            .setCommandId(command.commandId)
                            .setResultTime(LHUtil.fromDate(new Date()))
                            .setResult(ByteString.copyFrom(response.toBytes(config)))
                    )
                    .build();

                server.onResponseReceived(command.commandId, cmdReply);
            }
        } catch (Exception exn) {
            exn.printStackTrace();
            dao.abortChangesAndMarkWfRunFailed(exn.getMessage());
            // TODO: need to actually close off the response. Otherwise, the
            // request will hang until the CleanupOldWaiters() thing fires and
            // cleans stuff up.

            // Should we have a DLQ? I don't think that makes sense...the internals
            // of a database like Postgres don't have a DLQ for their WAL.
        }
    }
}
