package io.littlehorse.server.streams.topology.core.processors;

import com.google.protobuf.Message;
import io.grpc.StatusRuntimeException;
import io.littlehorse.common.AuthorizationContext;
import io.littlehorse.common.AuthorizationContextImpl;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.dao.CoreProcessorDAO;
import io.littlehorse.common.model.ClusterLevelCommand;
import io.littlehorse.common.model.corecommand.CommandModel;
import io.littlehorse.common.model.getable.global.acl.ServerACLModel;
import io.littlehorse.common.proto.WaitForCommandResponse;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.server.KafkaStreamsServerImpl;
import io.littlehorse.server.streams.ServerTopology;
import io.littlehorse.server.streams.store.ModelStore;
import io.littlehorse.server.streams.topology.core.CommandProcessorOutput;
import io.littlehorse.server.streams.topology.core.CoreProcessorDAOImpl;
import io.littlehorse.server.streams.util.HeadersUtil;
import io.littlehorse.server.streams.util.MetadataCache;
import java.time.Duration;
import java.util.Date;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.processor.PunctuationType;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.state.KeyValueStore;

@Slf4j
public class CommandProcessor implements Processor<String, CommandModel, String, CommandProcessorOutput> {

    private ProcessorContext<String, CommandProcessorOutput> ctx;
    private LHServerConfig config;
    private KafkaStreamsServerImpl server;
    private final MetadataCache metadataCache;

    private KeyValueStore<String, Bytes> nativeStore;

    private CommandDAOFactory daoFactory;

    public CommandProcessor(LHServerConfig config, KafkaStreamsServerImpl server, MetadataCache metadataCache) {
        this.config = config;
        this.server = server;
        this.metadataCache = metadataCache;
    }

    @Override
    public void init(final ProcessorContext<String, CommandProcessorOutput> ctx) {
        this.ctx = ctx;

        this.nativeStore = ctx.getStateStore(ServerTopology.CORE_STORE);
        daoFactory = new CommandDAOFactory();
        daoFactory.getDefaultDao(LHConstants.ANONYMOUS_PRINCIPAL).onPartitionClaimed();
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
        CoreProcessorDAO coreDao = daoFactory.fromMetadata(command, commandRecord.headers());
        coreDao.initCommand(command, this.nativeStore, commandRecord.headers());

        log.trace(
                "{} Processing command of type {} with commandId {} with partition key {}",
                config.getLHInstanceId(),
                command.type,
                command.commandId,
                command.getPartitionKey());

        try {
            Message response = command.process(coreDao, config);
            coreDao.commit();
            if (command.hasResponse() && command.getCommandId() != null) {
                WaitForCommandResponse cmdReply = WaitForCommandResponse.newBuilder()
                        .setCommandId(command.getCommandId())
                        .setResultTime(LHUtil.fromDate(new Date()))
                        .setResult(response.toByteString())
                        .build();

                server.onResponseReceived(command.getCommandId(), cmdReply);
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

    private final class CommandDAOFactory {

        CoreProcessorDAO fromMetadata(CommandModel command, Headers metadata) {
            String tenantId = HeadersUtil.tenantIdFromMetadata(metadata);
            String principalId = HeadersUtil.principalIdFromMetadata(metadata);
            return new CoreProcessorDAOImpl(
                    ctx,
                    config,
                    server,
                    metadataCache,
                    storeFor(command, tenantId, globalMetadata()),
                    storeFor(command, tenantId, coreStore()),
                    contextFor(principalId, tenantId));
        }

        CoreProcessorDAO getDefaultDao(String tenantId) {
            return new CoreProcessorDAOImpl(
                    ctx,
                    config,
                    server,
                    metadataCache,
                    ModelStore.defaultStore(globalMetadata()),
                    ModelStore.defaultStore(coreStore()),
                    defaultContext(tenantId));
        }

        private ModelStore storeFor(CommandModel command, String tenantId, KeyValueStore<String, Bytes> nativeStore) {
            ModelStore store;
            if (command.getSubCommand() instanceof ClusterLevelCommand) {
                store = ModelStore.defaultStore(nativeStore);
            } else {
                store = ModelStore.instanceFor(nativeStore, tenantId);
            }
            return store;
        }

        private KeyValueStore<String, Bytes> globalMetadata() {
            return ctx.getStateStore(ServerTopology.GLOBAL_METADATA_STORE);
        }

        private KeyValueStore<String, Bytes> coreStore() {
            return ctx.getStateStore(ServerTopology.CORE_STORE);
        }

        private AuthorizationContext contextFor(String principalId, String tenantId) {
            // TODO: for fine-grained acl verification we will need to find list of ACLS for the principalId and
            // tenantId
            List<ServerACLModel> currentAcls = List.of();
            return new AuthorizationContextImpl(principalId, tenantId, currentAcls);
        }

        private AuthorizationContext defaultContext(String principalId) {
            return new AuthorizationContextImpl(principalId, ModelStore.DEFAULT_TENANT, List.of());
        }
    }
}
