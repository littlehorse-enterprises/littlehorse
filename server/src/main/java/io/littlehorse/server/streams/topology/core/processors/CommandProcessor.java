package io.littlehorse.server.streams.topology.core.processors;

import com.google.protobuf.Message;
import io.grpc.StatusRuntimeException;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.model.corecommand.CommandModel;
import io.littlehorse.common.proto.WaitForCommandResponse;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.server.KafkaStreamsServerImpl;
import io.littlehorse.server.streams.ServerTopology;
import io.littlehorse.server.streams.store.ReadOnlyRocksDBWrapper;
import io.littlehorse.server.streams.store.RocksDBWrapper;
import io.littlehorse.server.streams.topology.core.CommandProcessorOutput;
import io.littlehorse.server.streams.topology.core.CoreProcessorDAOImpl;
import io.littlehorse.server.streams.util.MetadataCache;
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
    private CoreProcessorDAOImpl dao;
    private LHServerConfig config;
    private KafkaStreamsServerImpl server;
    private RocksDBWrapper rocksdb;
    private final MetadataCache metadataCache;

    public CommandProcessor(LHServerConfig config, KafkaStreamsServerImpl server, MetadataCache metadataCache) {
        this.config = config;
        this.server = server;
        this.metadataCache = metadataCache;
    }

    @Override
    public void init(final ProcessorContext<String, CommandProcessorOutput> ctx) {
        this.ctx = ctx;

        // Once multi-tenancy is implemented, we will pass the rocksdb store in
        // the initCommand() method. That way, we can isolate the tenants through
        // rocksdb without changing any business logic in common.model or in
        // CoreProcessorDAOImpl.
        this.rocksdb = new RocksDBWrapper(ctx.getStateStore(ServerTopology.CORE_STORE), config);

        ReadOnlyRocksDBWrapper globalStore =
                new ReadOnlyRocksDBWrapper(ctx.getStateStore(ServerTopology.GLOBAL_METADATA_STORE), config);

        dao = new CoreProcessorDAOImpl(this.ctx, config, server, metadataCache, rocksdb, globalStore);
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
        dao.initCommand(command);

        log.trace(
                "{} Processing command of type {} with commandId {} with partition key {}",
                config.getLHInstanceId(),
                command.type,
                command.commandId,
                command.getPartitionKey());

        try {
            Message response = command.process(dao, config);
            dao.commit();
            if (command.hasResponse() && command.commandId != null) {
                WaitForCommandResponse cmdReply = WaitForCommandResponse.newBuilder()
                        .setCommandId(command.commandId)
                        .setResultTime(LHUtil.fromDate(new Date()))
                        .setResult(response.toByteString())
                        .build();

                server.onResponseReceived(command.commandId, cmdReply);
            }
        } catch (Exception exn) {
            if (isUserError(exn)) {
                StatusRuntimeException sre = (StatusRuntimeException) exn;
                log.debug(
                        "Caught exception processing {}:\nStatus: {}\nDescription: {}\nCause: {}",
                        command.getType(),
                        sre.getStatus().getCode(),
                        sre.getStatus().getDescription(),
                        sre.getMessage(),
                        sre.getCause());
            } else {
                log.error("Caught exception processing command:", exn);
            }
            if (command.hasResponse() && command.getCommandId() != null) {
                server.sendErrorToClient(command.getCommandId(), exn);
            }

            // If we get here, then a Really Bad Thing has happened and we should
            // let the sysadmin of this LH Server know, and provide as much debugging
            // information as possible.
        }
    }

    private boolean isUserError(Exception exn) {
        if (StatusRuntimeException.class.isAssignableFrom(exn.getClass())) {
            StatusRuntimeException sre = (StatusRuntimeException) exn;

            switch (sre.getStatus().getCode()) {
                case NOT_FOUND,
                        INVALID_ARGUMENT,
                        ALREADY_EXISTS,
                        OUT_OF_RANGE,
                        PERMISSION_DENIED,
                        UNAUTHENTICATED,
                        FAILED_PRECONDITION,
                        // RESOURCE_EXHAUSTED used for quota violations.
                        RESOURCE_EXHAUSTED:
                    return true;

                case OK,
                        UNKNOWN,
                        UNIMPLEMENTED,
                        UNAVAILABLE,
                        INTERNAL,
                        DEADLINE_EXCEEDED,
                        DATA_LOSS,
                        ABORTED,
                        CANCELLED:
            }
        }
        return false;
    }

    private void forwardMetricsUpdates(long timestamp) {
        // TODO: batch and send metrics to the repartition processor
    }
}
