package io.littlehorse.common.model.command.subcommandresponse;

import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.model.meta.ExternalEventDef;
import io.littlehorse.common.proto.LHResponseCodePb;
import io.littlehorse.common.proto.PutExternalEventDefReplyPb;
import io.littlehorse.common.proto.PutExternalEventDefReplyPbOrBuilder;

public class PutExternalEventDefReply
    extends LHSerializable<PutExternalEventDefReplyPb> {

    public LHResponseCodePb code;
    public String message;
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
