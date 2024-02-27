package io.littlehorse.common.model.corecommand;

import com.google.protobuf.ByteString;
import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.proto.WaitForActionResponse;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.Date;

public class WaitForActionResponseModel extends LHSerializable<WaitForActionResponse> {

    public String actionId;
    public Date resultTime;
    public byte[] result;

    public Class<WaitForActionResponse> getProtoBaseClass() {
        return WaitForActionResponse.class;
    }

    public WaitForActionResponse.Builder toProto() {
        WaitForActionResponse.Builder out = WaitForActionResponse.newBuilder()
                .setActionId(actionId)
                .setResult(ByteString.copyFrom(result))
                .setResultTime(LHUtil.fromDate(resultTime));
        return out;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        WaitForActionResponse p = (WaitForActionResponse) proto;
        actionId = p.getActionId();
        resultTime = LHUtil.fromProtoTs(p.getResultTime());
        result = p.getResult().toByteArray();
    }

    public String getStoreKey() {
        return actionId;
    }
}
