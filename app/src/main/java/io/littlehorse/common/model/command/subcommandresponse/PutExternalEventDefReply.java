package io.littlehorse.common.model.command.subcommandresponse;

import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.model.command.AbstractResponse;
import io.littlehorse.common.model.meta.ExternalEventDef;
import io.littlehorse.jlib.common.proto.PutExternalEventDefReplyPb;
import io.littlehorse.jlib.common.proto.PutExternalEventDefReplyPbOrBuilder;

public class PutExternalEventDefReply
    extends AbstractResponse<PutExternalEventDefReplyPb> {

    public ExternalEventDef result;

    public Class<PutExternalEventDefReplyPb> getProtoBaseClass() {
        return PutExternalEventDefReplyPb.class;
    }

    public PutExternalEventDefReplyPb.Builder toProto() {
        PutExternalEventDefReplyPb.Builder out = PutExternalEventDefReplyPb.newBuilder();
        out.setCode(code);
        if (message != null) out.setMessage(message);
        if (result != null) out.setResult(result.toProto());
        return out;
    }

    public void initFrom(MessageOrBuilder proto) {
        PutExternalEventDefReplyPbOrBuilder p = (PutExternalEventDefReplyPbOrBuilder) proto;
        code = p.getCode();
        if (p.hasMessage()) message = p.getMessage();
        if (p.hasResult()) result = ExternalEventDef.fromProto(p.getResult());
    }
}
