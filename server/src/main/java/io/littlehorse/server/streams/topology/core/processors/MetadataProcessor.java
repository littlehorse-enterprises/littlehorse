package io.littlehorse.server.streams.topology.core.processors;

import com.google.protobuf.Message;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.model.metadatacommand.MetadataCommandModel;
import io.littlehorse.common.proto.MetadataCommand;
import io.littlehorse.common.proto.WaitForCommandResponse;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.server.KafkaStreamsServerImpl;
import io.littlehorse.server.streams.topology.core.MetadataCommandExecution;
import io.littlehorse.server.streams.util.MetadataCache;
import java.util.Date;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;

/*
 * This is the processor that validates and processes commands to update metadata,
 * such as WfSpec/TaskDef/ExternalEventDef/UserTaskDef.
 */
@Slf4j
public class MetadataProcessor implements Processor<String, MetadataCommand, String, Bytes> {

    private final LHServerConfig config;
    private final KafkaStreamsServerImpl server;
    private final MetadataCache metadataCache;

    private ProcessorContext<String, Bytes> ctx;

    public MetadataProcessor(LHServerConfig config, KafkaStreamsServerImpl server, MetadataCache metadataCache) {
        this.config = config;
        this.server = server;
        this.metadataCache = metadataCache;
    }

    public void init(final ProcessorContext<String, Bytes> ctx) {
        this.ctx = ctx;
    }

    @Override
    public void process(final Record<String, MetadataCommand> commandRecord) {
        // We have another wrapper here as a guard against a poison pill (even
        // though we test extensively to prevent poison pills, it's better
        // to be safe than sorry.)
        try {
            processHelper(commandRecord);
        } catch (Exception exn) {
            log.error("Unexpected error processing record: ", exn);
        }
    }

    public void processHelper(final Record<String, MetadataCommand> record) {
        MetadataCommandExecution metadataContext = buildContext(record);
        MetadataCommandModel command = metadataContext.currentCommand();
        log.trace(
                "{} Processing command of type {} with commandId {}",
                config.getLHInstanceId(),
                command.getType(),
                command.getCommandId());

        try {
            Message response = command.process(metadataContext);
            if (command.hasResponse() && command.getCommandId() != null) {
                WaitForCommandResponse cmdReply = WaitForCommandResponse.newBuilder()
                        .setCommandId(command.getCommandId())
                        .setResultTime(LHUtil.fromDate(new Date()))
                        .setResult(response.toByteString())
                        .build();

                server.onResponseReceived(command.getCommandId(), cmdReply);

                // This allows us to set a larger commit interval for the Core Topology
                // without affecting latency of updates to the metadata global store.
                //
                // Messages coming through this processor are very low-throughput (updating
                // WfSpec is much less common than running a WfRun), so calling a premature
                // commit will not impact performance of the rest of the application very
                // often.
                ctx.commit();
            }
        } catch (Exception exn) {
            log.error("Caught exception processing {} command: {}", command.getType(), exn);
            if (command.hasResponse() && command.getCommandId() != null) {
                server.sendErrorToClient(command.getCommandId(), exn);
            }

            // If we get here, then a Really Bad Thing has happened and we should
            // let the sysadmin of this LH Server know, and provide as much debugging
            // information as possible.
        }
    }

    public MetadataCommandExecution buildContext(final Record<String, MetadataCommand> record) {
        Headers recordMetadata = record.headers();
        return new MetadataCommandExecution(recordMetadata, ctx, metadataCache, config, record.value());
    }
}
