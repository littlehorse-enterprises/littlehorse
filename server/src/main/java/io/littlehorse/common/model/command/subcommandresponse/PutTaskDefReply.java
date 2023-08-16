package io.littlehorse.common.model.command.subcommandresponse;

import com.google.protobuf.Message;
import io.littlehorse.common.model.command.AbstractResponse;
import io.littlehorse.common.model.meta.TaskDefModel;
import io.littlehorse.sdk.common.proto.PutTaskDefReplyPb;

public class PutTaskDefReply extends AbstractResponse<PutTaskDefReplyPb> {

    public TaskDefModel result;

    public Class<PutTaskDefReplyPb> getProtoBaseClass() {
        return PutTaskDefReplyPb.class;
    }

    public PutTaskDefReplyPb.Builder toProto() {
        PutTaskDefReplyPb.Builder out = PutTaskDefReplyPb.newBuilder();
        out.setCode(code);
        if (message != null) out.setMessage(message);
        if (result != null) out.setResult(result.toProto());
        return out;
    }

    public void initFrom(Message proto) {
        PutTaskDefReplyPb p = (PutTaskDefReplyPb) proto;
        code = p.getCode();
        if (p.hasMessage()) message = p.getMessage();
        if (p.hasResult()) result = TaskDefModel.fromProto(p.getResult());
    }
}
