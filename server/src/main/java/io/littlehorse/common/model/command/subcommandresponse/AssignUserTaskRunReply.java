package io.littlehorse.common.model.command.subcommandresponse;

import com.google.protobuf.Message;
import io.littlehorse.common.model.command.AbstractResponse;
import io.littlehorse.common.model.meta.ExternalEventDef;
import io.littlehorse.sdk.common.proto.AssignUserTaskRunReplyPb;

public class AssignUserTaskRunReply
    extends AbstractResponse<AssignUserTaskRunReplyPb> {

    public ExternalEventDef result;

    public Class<AssignUserTaskRunReplyPb> getProtoBaseClass() {
        return AssignUserTaskRunReplyPb.class;
    }

    public AssignUserTaskRunReplyPb.Builder toProto() {
        AssignUserTaskRunReplyPb.Builder out = AssignUserTaskRunReplyPb.newBuilder();
        out.setCode(code);
        if (message != null) out.setMessage(message);
        return out;
    }

    public void initFrom(Message proto) {
        AssignUserTaskRunReplyPb p = (AssignUserTaskRunReplyPb) proto;
        code = p.getCode();
        if (p.hasMessage()) message = p.getMessage();
    }
}
