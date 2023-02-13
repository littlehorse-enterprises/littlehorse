package io.littlehorse.common.model.command.subcommandresponse;

import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.model.command.AbstractResponse;
import io.littlehorse.jlib.common.proto.StopWfRunReplyPb;
import io.littlehorse.jlib.common.proto.StopWfRunReplyPbOrBuilder;

public class StopWfRunReply extends AbstractResponse<StopWfRunReplyPb> {

    public Class<StopWfRunReplyPb> getProtoBaseClass() {
        return StopWfRunReplyPb.class;
    }

    public StopWfRunReplyPb.Builder toProto() {
        StopWfRunReplyPb.Builder out = StopWfRunReplyPb.newBuilder();
        out.setCode(code);
        if (message != null) {
            out.setMessage(message);
        }
        return out;
    }

    public void initFrom(MessageOrBuilder proto) {
        StopWfRunReplyPbOrBuilder p = (StopWfRunReplyPbOrBuilder) proto;
        code = p.getCode();
        if (p.hasMessage()) message = p.getMessage();
    }

    public static StopWfRunReply fromProto(StopWfRunReplyPbOrBuilder p) {
        StopWfRunReply out = new StopWfRunReply();
        out.initFrom(p);
        return out;
    }
}
