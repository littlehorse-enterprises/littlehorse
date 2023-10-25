package io.littlehorse.server.streams.topology.core.processors;

import com.google.protobuf.Message;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.ServerContext;
import io.littlehorse.common.ServerContextImpl;
import io.littlehorse.common.dao.MetadataProcessorDAO;
import io.littlehorse.common.model.ServerSubCommand;
import io.littlehorse.common.model.metadatacommand.MetadataCommandModel;
import io.littlehorse.common.proto.WaitForCommandResponse;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.server.KafkaStreamsServerImpl;
import io.littlehorse.server.streams.ServerTopology;
import io.littlehorse.server.streams.store.ModelStore;
import io.littlehorse.server.streams.topology.core.MetadataProcessorDAOImpl;
import io.littlehorse.server.streams.util.MetadataCache;
import java.util.Date;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.state.KeyValueStore;

/*
 * This is the processor that validates and processes commands to update metadata,
 * such as WfSpec/TaskDef/ExternalEventDef/UserTaskDef.
 */
@Slf4j
public class MetadataProcessor implements Processor<String, MetadataCommandModel, String, Bytes> {

    private final LHServerConfig config;
    private final KafkaStreamsServerImpl server;
    private final MetadataCache metadataCache;

    private ProcessorContext<String, Bytes> ctx;

    private MetadataDAOFactory daoFactory;

    public MetadataProcessor(LHServerConfig config, KafkaStreamsServerImpl server, MetadataCache metadataCache) {
        this.config = config;
        this.server = server;
        this.metadataCache = metadataCache;
    }

    public void init(final ProcessorContext<String, Bytes> ctx) {
        this.ctx = ctx;
        this.daoFactory = new MetadataDAOFactory();
    }

    @Override
    public void process(final Record<String, MetadataCommandModel> commandRecord) {
        // We have another wrapper here as a guard against a poison pill (even
        // though we test extensively to prevent poison pills, it's better
        // to be safe than sorry.)
        try {
            processHelper(commandRecord);
        } catch (Exception exn) {
            log.error("Unexpected error processing record: ", exn);
        }
    }

    public void processHelper(final Record<String, MetadataCommandModel> record) {
        MetadataCommandModel command = record.value();
        MetadataProcessorDAO dao = this.daoFactory.getDao(command);
        log.trace(
                "{} Processing command of type {} with commandId {}",
                config.getLHInstanceId(),
                command.getType(),
                command.getCommandId());

        try {
            dao.initCommand(command);
            Message response = command.process(dao, config);
            if (command.hasResponse() && command.getCommandId() != null) {
                WaitForCommandResponse cmdReply = WaitForCommandResponse.newBuilder()
                        .setCommandId(command.getCommandId())
                        .setResultTime(LHUtil.fromDate(new Date()))
                        .setResult(response.toByteString())
                        .build();

                server.onResponseReceived(command.getCommandId(), cmdReply);
            }
        } catch (Exception exn) {
            log.error("Caught exception processing command: {}", exn);
            if (command.hasResponse() && command.getCommandId() != null) {
                server.sendErrorToClient(command.getCommandId(), exn);
            }

            // If we get here, then a Really Bad Thing has happened and we should
            // let the sysadmin of this LH Server know, and provide as much debugging
            // information as possible.
        }
    }

    private final class MetadataDAOFactory {

        private final KeyValueStore<String, Bytes> nativeMetadataStore;

        MetadataDAOFactory() {
            nativeMetadataStore = ctx.getStateStore(ServerTopology.METADATA_STORE);
        }

        MetadataProcessorDAO getDao(MetadataCommandModel command) {
            return new MetadataProcessorDAOImpl(
                    storeFor(command),
                    metadataCache,
                    new ServerContextImpl(command.getTenantId(), ServerContext.Scope.PROCESSOR));
        }

        private ModelStore storeFor(MetadataCommandModel command) {
            ModelStore store;
            if (command.getSubCommand() instanceof ServerSubCommand) {
                store = ModelStore.defaultStore(nativeMetadataStore);
            } else {
                store = ModelStore.instanceFor(nativeMetadataStore, command.getTenantId());
            }
            return store;
        }
    }
}
