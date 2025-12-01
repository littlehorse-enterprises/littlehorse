package io.littlehorse.server.streams.topology.core;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import io.littlehorse.common.model.outputtopic.OutputTopicRecordModel;
import io.littlehorse.common.proto.Command;
import lombok.Getter;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.streams.processor.api.Record;

import java.time.Duration;
import java.util.Date;

@Getter
public class CommandProcessorOutput {

    private final Message command;
    private final String partitionKey;
    private final Date maturationTime;
    private final TenantIdModel tenantId;

    private CommandProcessorOutput(Message command, String partitionKey, Date maturationTime, TenantIdModel tenantId) {
        this.command = command;
        this.partitionKey = partitionKey;
        this.maturationTime = maturationTime;
        this.tenantId = tenantId;
    }

    public static CommandProcessorOutput timer(Command commandToExecute, Date maturationTime) {
        return new CommandProcessorOutput(commandToExecute, null, maturationTime, null);
    }

    public static CommandProcessorOutput repartition(LHSerializable<?> commandToExecute, String partitionKey) {
        return new CommandProcessorOutput(commandToExecute.toProto().build(), partitionKey, null, null);
    }

    public static CommandProcessorOutput outputRecord(OutputTopicRecordModel thingToSend, String partitionKey, TenantIdModel tenant) {
        return new CommandProcessorOutput(thingToSend.toProto().build(), partitionKey, null, tenant);
    }

    public Record<String, CommandProcessorOutput> toRecord(Headers metadata) {
        return new Record<>(partitionKey, this, System.currentTimeMillis(), metadata);
    }

    public boolean isTimer() {
        return maturationTime != null;
    }

}
