package io.littlehorse.common.model.corecommand;

import com.google.protobuf.ByteString;
import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.proto.WaitForPedroResponse;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.Date;

public class WaitForCommandResultModel extends LHSerializable<WaitForPedroResponse> {

    public String commandId;
    public Date resultTime;
    public byte[] result;

    public Class<WaitForPedroResponse> getProtoBaseClass() {
        return WaitForPedroResponse.class;
    }

    public WaitForPedroResponse.Builder toProto() {
        WaitForPedroResponse.Builder out = WaitForPedroResponse.newBuilder()
                .setCommandId(commandId)
                .setResult(ByteString.copyFrom(result))
                .setResultTime(LHUtil.fromDate(resultTime));
        return out;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        WaitForPedroResponse p = (WaitForPedroResponse) proto;
        commandId = p.getCommandId();
        resultTime = LHUtil.fromProtoTs(p.getResultTime());
        result = p.getResult().toByteArray();
    }

    public String getStoreKey() {
        return commandId;
    }
}
