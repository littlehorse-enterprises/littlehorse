package io.littlehorse.common.model.command.subcommandresponse;

import com.google.protobuf.Message;
import io.littlehorse.common.model.command.AbstractResponse;
import io.littlehorse.sdk.common.proto.CompleteUserTaskRunResponse;

public class CompleteUserTaskRunReply extends AbstractResponse<CompleteUserTaskRunResponse> {

    public Class<CompleteUserTaskRunResponse> getProtoBaseClass() {
        return CompleteUserTaskRunResponse.class;
    }

    public CompleteUserTaskRunResponse.Builder toProto() {
        CompleteUserTaskRunResponse.Builder out = CompleteUserTaskRunResponse.newBuilder();
        out.setCode(code);
        if (message != null) out.setMessage(message);
        return out;
    }

    public void initFrom(Message proto) {
        CompleteUserTaskRunResponse p = (CompleteUserTaskRunResponse) proto;
        code = p.getCode();
        if (p.hasMessage()) message = p.getMessage();
    }
}
