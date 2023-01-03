package io.littlehorse.server.streamsimpl.coreprocessors;

import com.google.protobuf.ByteString;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.model.command.AbstractResponse;
import io.littlehorse.common.model.command.Command;
import io.littlehorse.common.proto.CommandResultPb;
import io.littlehorse.common.proto.StoreQueryStatusPb;
import io.littlehorse.common.proto.WaitForCommandReplyPb;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.server.KafkaStreamsServerImpl;
import java.time.Duration;
import java.util.Date;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.processor.PunctuationType;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;

public class CommandProcessor
    implements Processor<String, Command, String, CommandProcessorOutput> {

    private ProcessorContext<String, CommandProcessorOutput> ctx;
    private KafkaStreamsLHDAOImpl dao;
    private LHConfig config;
    private KafkaStreamsServerImpl server;

    // private String claimGuid;

    public CommandProcessor(LHConfig config, KafkaStreamsServerImpl server) {
        this.config = config;
        this.server = server;
        // this.claimGuid = LHUtil.generateGuid();
    }

    @Override
    public void init(final ProcessorContext<String, CommandProcessorOutput> ctx) {
        // // claim the thing
        // ProducerRecord<String, Bytes> claimRecord = new ProducerRecord<String, Bytes>(
        //     config.getCoreCmdTopicName(),
        //     ctx.taskId().partition(),
        //     LHConstants.PARTITION_CLAIM_KEY,
        //     null
        // );
        // claimRecord
        //     .headers()
        //     .add(LHConstants.PARTITION_CLAIM_GUID_HEADER, claimGuid.getBytes());

        // server.getProducer().sendRecord(claimRecord, null);

        // temporary hack

        this.ctx = ctx;
        dao = new KafkaStreamsLHDAOImpl(this.ctx, config, server);
        // ctx.schedule(
        //     Duration.ofMillis(100),
        //     PunctuationType.WALL_CLOCK_TIME,
        //     dao::broadcastTagCounts
        // );
        dao.onPartitionClaimed();
    }

    @Override
    public void process(final Record<String, Command> commandRecord) {
        // if (commandRecord.key().equals(LHConstants.PARTITION_CLAIM_KEY)) {
        //     Iterable<Header> iter = commandRecord
        //         .headers()
        //         .headers(LHConstants.PARTITION_CLAIM_GUID_HEADER);

        //     Header h = iter.iterator().next();

        //     if (new String(h.value()).equals(claimGuid)) {
        //         dao.onPartitionClaimed();
        //     } else {
        //         LHUtil.log("Got a stale claim! Doing nothing.");
        //     }
        //     return;
        // }

        try {
            Command command = commandRecord.value();
            dao.setCommand(command);
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
            dao.abortChanges();
            // Should we have a DLQ? I don't think that makes sense...the internals
            // of a database like Postgres don't have a DQL for their WAL.
        }
    }
}
