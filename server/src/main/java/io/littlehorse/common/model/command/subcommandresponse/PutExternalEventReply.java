package io.littlehorse.common.model.command.subcommandresponse;

import com.google.protobuf.Message;
import io.littlehorse.common.model.command.AbstractResponse;
import io.littlehorse.common.model.wfrun.ExternalEvent;
import io.littlehorse.jlib.common.proto.PutExternalEventReplyPb;

public class PutExternalEventReply extends AbstractResponse<PutExternalEventReplyPb> {

    public ExternalEvent result;

    public Class<PutExternalEventReplyPb> getProtoBaseClass() {
        return PutExternalEventReplyPb.class;
    }

    public PutExternalEventReplyPb.Builder toProto() {
        PutExternalEventReplyPb.Builder out = PutExternalEventReplyPb
            .newBuilder()
            .setCode(code);
        if (message != null) out.setMessage(message);
        if (result != null) out.setResult(result.toProto());

        return out;
    }

    public void initFrom(Message proto) {
        PutExternalEventReplyPb p = (PutExternalEventReplyPb) proto;
        code = p.getCode();
        if (p.hasMessage()) message = p.getMessage();
        if (p.hasResult()) result = ExternalEvent.fromProto(p.getResult());
    }
}
