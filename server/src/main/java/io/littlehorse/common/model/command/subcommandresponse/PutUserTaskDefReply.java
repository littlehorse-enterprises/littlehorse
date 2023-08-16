package io.littlehorse.common.model.command.subcommandresponse;

import com.google.protobuf.Message;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.model.command.AbstractResponse;
import io.littlehorse.common.model.meta.usertasks.UserTaskDefModel;
import io.littlehorse.sdk.common.proto.PutUserTaskDefReplyPb;

public class PutUserTaskDefReply extends AbstractResponse<PutUserTaskDefReplyPb> {

    public UserTaskDefModel result;

    public Class<PutUserTaskDefReplyPb> getProtoBaseClass() {
        return PutUserTaskDefReplyPb.class;
    }

    public PutUserTaskDefReplyPb.Builder toProto() {
        PutUserTaskDefReplyPb.Builder out = PutUserTaskDefReplyPb.newBuilder();
        out.setCode(code);
        if (message != null) out.setMessage(message);
        if (result != null) out.setResult(result.toProto());
        return out;
    }

    public void initFrom(Message proto) {
        PutUserTaskDefReplyPb p = (PutUserTaskDefReplyPb) proto;
        code = p.getCode();
        if (p.hasMessage()) message = p.getMessage();
        if (p.hasResult()) result =
            LHSerializable.fromProto(p.getResult(), UserTaskDefModel.class);
    }
}
