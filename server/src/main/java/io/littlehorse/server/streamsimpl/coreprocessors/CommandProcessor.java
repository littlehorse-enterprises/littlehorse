package io.littlehorse.server.streamsimpl.coreprocessors;

import com.google.protobuf.ByteString;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.model.command.AbstractResponse;
import io.littlehorse.common.model.command.CommandModel;
import io.littlehorse.common.proto.CommandResultPb;
import io.littlehorse.common.proto.StoreQueryStatusPb;
import io.littlehorse.common.proto.WaitForCommandResponse;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.server.KafkaStreamsServerImpl;
import io.littlehorse.server.streamsimpl.storeinternals.LHStoreWrapper;
import io.littlehorse.server.streamsimpl.util.WfSpecCache;
import java.time.Duration;
import java.util.Date;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.processor.PunctuationType;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;

@Slf4j
public class CommandProcessor implements Processor<String, CommandModel, String, CommandProcessorOutput> {

    private ProcessorContext<String, CommandProcessorOutput> ctx;
    private KafkaStreamsLHDAOImpl dao;
    private LHConfig config;
    private KafkaStreamsServerImpl server;
    private final WfSpecCache wfSpecCache;

    private final String storeName;

    private final boolean isMetadataProcessorInstance;

    public CommandProcessor(
            LHConfig config,
            KafkaStreamsServerImpl server,
            WfSpecCache wfSpecCache,
            String storeName,
            boolean isMetadataProcessorInstance) {
        this.config = config;
        this.server = server;
        this.wfSpecCache = wfSpecCache;
        this.storeName = storeName;
        this.isMetadataProcessorInstance = isMetadataProcessorInstance;
    }

    @Override
    public void init(final ProcessorContext<String, CommandProcessorOutput> ctx) {
        // temporary hack

        this.ctx = ctx;
        final LHStoreWrapper localStore = new LHStoreWrapper(ctx.getStateStore(storeName), config);
        dao = new KafkaStreamsLHDAOImpl(this.ctx, config, server, wfSpecCache, localStore, isMetadataProcessorInstance);
        dao.onPartitionClaimed();

        ctx.schedule(Duration.ofSeconds(30), PunctuationType.WALL_CLOCK_TIME, this::forwardMetricsUpdates);
    }

    @Override
    public void process(final Record<String, CommandModel> commandRecord) {
        // We have another wrapper here as a guard against a poison pill (even
        // though we test extensively to prevent poison pills, it's better
        // to be safe than sorry.)
        try {
            processHelper(commandRecord);
        } catch (Exception exn) {
            log.error("Unexpected error processing record: ", exn);
        }
    }

    private void processHelper(final Record<String, CommandModel> commandRecord) {
        CommandModel command = commandRecord.value();
        dao.setCommand(command);

        log.trace(
                "{} Processing command of type {} with commandId {} on partition {}",
                config.getLHInstanceId(),
                command.type,
                command.commandId,
                command.getPartitionKey());

        try {
            AbstractResponse<?> response = command.process(dao, config);
            dao.commitChanges();
            if (command.hasResponse() && command.commandId != null) {
                WaitForCommandResponse cmdReply = WaitForCommandResponse.newBuilder()
                        .setCode(StoreQueryStatusPb.RSQ_OK)
                        .setResult(CommandResultPb.newBuilder()
                                .setCommandId(command.commandId)
                                .setResultTime(LHUtil.fromDate(new Date()))
                                .setResult(ByteString.copyFrom(response.toBytes(config))))
                        .build();

                server.onResponseReceived(command.commandId, cmdReply);
            }
        } catch (Exception exn) {
            log.error("Caught exception processing command: {}", exn);
            if (command.hasResponse() && command.getCommandId() != null) {
                server.sendErrorToClient(command.getCommandId(), exn);
            }
            // Note that command id's are wfRunId's too.
            dao.abortChangesAndMarkWfRunFailed(exn, command.getPartitionKey());
            // Should we have a DLQ? I don't think that makes sense...the internals
            // of a database like Postgres don't have a DLQ for their WAL. However,
            // we should add metrics. If we get here, then A Very Bad Thing has
            // happened and the LH Server should know about it.
        }
    }

    private void forwardMetricsUpdates(long timestamp) {
        dao.forwardAndClearMetricsUpdatesUntil();
    }
}
