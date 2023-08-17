package io.littlehorse.common.model.command.subcommandresponse;

import com.google.protobuf.Message;
import io.littlehorse.common.model.command.AbstractResponse;
import io.littlehorse.common.model.meta.ExternalEventDefModel;
import io.littlehorse.sdk.common.proto.AssignUserTaskRunResponse;

public class AssignUserTaskRunReply
    extends AbstractResponse<AssignUserTaskRunResponse> {

    public ExternalEventDefModel result;

    public Class<AssignUserTaskRunResponse> getProtoBaseClass() {
        return AssignUserTaskRunResponse.class;
    }

    public AssignUserTaskRunResponse.Builder toProto() {
        AssignUserTaskRunResponse.Builder out = AssignUserTaskRunResponse.newBuilder();
        out.setCode(code);
        if (message != null) out.setMessage(message);
        return out;
    }

    public void initFrom(Message proto) {
        AssignUserTaskRunResponse p = (AssignUserTaskRunResponse) proto;
        code = p.getCode();
        if (p.hasMessage()) message = p.getMessage();
    }
}
