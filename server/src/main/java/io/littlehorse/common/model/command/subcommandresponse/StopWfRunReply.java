package io.littlehorse.common.model.command.subcommandresponse;

import com.google.protobuf.Message;
import io.littlehorse.common.model.command.AbstractResponse;
import io.littlehorse.sdk.common.proto.StopWfRunResponse;

public class StopWfRunReply extends AbstractResponse<StopWfRunResponse> {

    public Class<StopWfRunResponse> getProtoBaseClass() {
        return StopWfRunResponse.class;
    }

    public StopWfRunResponse.Builder toProto() {
        StopWfRunResponse.Builder out = StopWfRunResponse.newBuilder();
        out.setCode(code);
        if (message != null) {
            out.setMessage(message);
        }
        return out;
    }

    public void initFrom(Message proto) {
        StopWfRunResponse p = (StopWfRunResponse) proto;
        code = p.getCode();
        if (p.hasMessage()) message = p.getMessage();
    }

    public static StopWfRunReply fromProto(StopWfRunResponse p) {
        StopWfRunReply out = new StopWfRunReply();
        out.initFrom(p);
        return out;
    }
}
