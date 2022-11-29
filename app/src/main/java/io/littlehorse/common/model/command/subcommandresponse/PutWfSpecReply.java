package io.littlehorse.common.model.command.subcommandresponse;

import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.model.command.SubCommandResponse;
import io.littlehorse.common.model.meta.WfSpec;
import io.littlehorse.common.proto.PutWfSpecReplyPb;
import io.littlehorse.common.proto.PutWfSpecReplyPbOrBuilder;

public class PutWfSpecReply extends SubCommandResponse<PutWfSpecReplyPb> {

    public WfSpec result;

    public Class<PutWfSpecReplyPb> getProtoBaseClass() {
        return PutWfSpecReplyPb.class;
    }

    public void initFrom(MessageOrBuilder proto) {
        PutWfSpecReplyPbOrBuilder p = (PutWfSpecReplyPbOrBuilder) proto;

        code = p.getCode();
        if (p.hasMessage()) message = p.getMessage();
        if (p.hasResult()) result = WfSpec.fromProto(p.getResult());
    }

    public PutWfSpecReplyPb.Builder toProto() {
        PutWfSpecReplyPb.Builder out = PutWfSpecReplyPb.newBuilder();
        out.setCode(code);
        if (message != null) out.setMessage(message);
        if (result != null) out.setResult(result.toProto());
        return out;
    }
}
