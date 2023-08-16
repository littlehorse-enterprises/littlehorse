package io.littlehorse.common.model.command.subcommandresponse;

import com.google.protobuf.Message;
import io.littlehorse.common.model.command.AbstractResponse;
import io.littlehorse.common.model.meta.WfSpecModel;
import io.littlehorse.sdk.common.proto.PutWfSpecReplyPb;

public class PutWfSpecReply extends AbstractResponse<PutWfSpecReplyPb> {

    public WfSpecModel result;

    public Class<PutWfSpecReplyPb> getProtoBaseClass() {
        return PutWfSpecReplyPb.class;
    }

    public void initFrom(Message proto) {
        PutWfSpecReplyPb p = (PutWfSpecReplyPb) proto;

        code = p.getCode();
        if (p.hasMessage()) message = p.getMessage();
        if (p.hasResult()) result = WfSpecModel.fromProto(p.getResult());
    }

    public PutWfSpecReplyPb.Builder toProto() {
        PutWfSpecReplyPb.Builder out = PutWfSpecReplyPb.newBuilder();
        out.setCode(code);
        if (message != null) out.setMessage(message);
        if (result != null) out.setResult(result.toProto());
        return out;
    }
}
