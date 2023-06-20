package io.littlehorse.common.model.command.subcommandresponse;

import com.google.protobuf.Message;
import io.littlehorse.common.model.command.AbstractResponse;
import io.littlehorse.jlib.common.proto.CompleteUserTaskRunReplyPb;

public class CompleteUserTaskRunReply
    extends AbstractResponse<CompleteUserTaskRunReplyPb> {

    public Class<CompleteUserTaskRunReplyPb> getProtoBaseClass() {
        return CompleteUserTaskRunReplyPb.class;
    }

    public CompleteUserTaskRunReplyPb.Builder toProto() {
        CompleteUserTaskRunReplyPb.Builder out = CompleteUserTaskRunReplyPb.newBuilder();
        out.setCode(code);
        if (message != null) out.setMessage(message);
        return out;
    }

    public void initFrom(Message proto) {
        CompleteUserTaskRunReplyPb p = (CompleteUserTaskRunReplyPb) proto;
        code = p.getCode();
        if (p.hasMessage()) message = p.getMessage();
    }
}
