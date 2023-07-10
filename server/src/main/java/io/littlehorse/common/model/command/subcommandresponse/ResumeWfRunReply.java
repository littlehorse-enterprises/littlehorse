package io.littlehorse.common.model.command.subcommandresponse;

import com.google.protobuf.Message;
import io.littlehorse.common.model.command.AbstractResponse;
import io.littlehorse.sdk.common.proto.ResumeWfRunReplyPb;

public class ResumeWfRunReply extends AbstractResponse<ResumeWfRunReplyPb> {

    public Class<ResumeWfRunReplyPb> getProtoBaseClass() {
        return ResumeWfRunReplyPb.class;
    }

    public ResumeWfRunReplyPb.Builder toProto() {
        ResumeWfRunReplyPb.Builder out = ResumeWfRunReplyPb.newBuilder();
        out.setCode(code);
        if (message != null) {
            out.setMessage(message);
        }
        return out;
    }

    public void initFrom(Message proto) {
        ResumeWfRunReplyPb p = (ResumeWfRunReplyPb) proto;
        code = p.getCode();
        if (p.hasMessage()) message = p.getMessage();
    }

    public static ResumeWfRunReply fromProto(ResumeWfRunReplyPb p) {
        ResumeWfRunReply out = new ResumeWfRunReply();
        out.initFrom(p);
        return out;
    }
}
