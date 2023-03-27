package io.littlehorse.common.model.command;

import com.google.protobuf.ByteString;
import com.google.protobuf.Message;
import io.littlehorse.common.model.Storeable;
import io.littlehorse.common.proto.CommandResultPb;
import io.littlehorse.common.util.LHUtil;
import java.util.Date;

// TODO: May need to find a more appropriate directory for this file.
public class CommandResult extends Storeable<CommandResultPb> {

    public String commandId;
    public Date resultTime;
    public byte[] result;

    public Class<CommandResultPb> getProtoBaseClass() {
        return CommandResultPb.class;
    }

    public CommandResultPb.Builder toProto() {
        CommandResultPb.Builder out = CommandResultPb
            .newBuilder()
            .setCommandId(commandId)
            .setResult(ByteString.copyFrom(result))
            .setResultTime(LHUtil.fromDate(resultTime));
        return out;
    }

    public void initFrom(Message proto) {
        CommandResultPb p = (CommandResultPb) proto;
        commandId = p.getCommandId();
        resultTime = LHUtil.fromProtoTs(p.getResultTime());
        result = p.getResult().toByteArray();
    }

    public String getStoreKey() {
        return commandId;
    }
}
