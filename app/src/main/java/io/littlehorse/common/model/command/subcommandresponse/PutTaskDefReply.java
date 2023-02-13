package io.littlehorse.common.model.command.subcommandresponse;

import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.model.command.AbstractResponse;
import io.littlehorse.common.model.meta.TaskDef;
import io.littlehorse.jlib.common.proto.PutTaskDefReplyPb;
import io.littlehorse.jlib.common.proto.PutTaskDefReplyPbOrBuilder;

public class PutTaskDefReply extends AbstractResponse<PutTaskDefReplyPb> {

    public TaskDef result;

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

    public void initFrom(MessageOrBuilder proto) {
        PutTaskDefReplyPbOrBuilder p = (PutTaskDefReplyPbOrBuilder) proto;
        code = p.getCode();
        if (p.hasMessage()) message = p.getMessage();
        if (p.hasResult()) result = TaskDef.fromProto(p.getResult());
    }
}
