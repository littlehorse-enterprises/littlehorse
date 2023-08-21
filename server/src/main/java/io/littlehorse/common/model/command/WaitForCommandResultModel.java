package io.littlehorse.common.model.command;

import com.google.protobuf.ByteString;
import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.proto.WaitForCommandResponse;
import io.littlehorse.common.util.LHUtil;
import java.util.Date;

public class WaitForCommandResultModel extends LHSerializable<WaitForCommandResponse> {

    public String commandId;
    public Date resultTime;
    public byte[] result;

    public Class<WaitForCommandResponse> getProtoBaseClass() {
        return WaitForCommandResponse.class;
    }

    public WaitForCommandResponse.Builder toProto() {
        WaitForCommandResponse.Builder out = WaitForCommandResponse.newBuilder()
                .setCommandId(commandId)
                .setResult(ByteString.copyFrom(result))
                .setResultTime(LHUtil.fromDate(resultTime));
        return out;
    }

    public void initFrom(Message proto) {
        WaitForCommandResponse p = (WaitForCommandResponse) proto;
        commandId = p.getCommandId();
        resultTime = LHUtil.fromProtoTs(p.getResultTime());
        result = p.getResult().toByteArray();
    }

    public String getStoreKey() {
        return commandId;
    }
}
