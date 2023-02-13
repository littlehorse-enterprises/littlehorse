package io.littlehorse.common.model.command.subcommandresponse;

import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.model.command.AbstractResponse;
import io.littlehorse.jlib.common.proto.ResumeWfRunReplyPb;
import io.littlehorse.jlib.common.proto.ResumeWfRunReplyPbOrBuilder;

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

    public void initFrom(MessageOrBuilder proto) {
        ResumeWfRunReplyPbOrBuilder p = (ResumeWfRunReplyPbOrBuilder) proto;
        code = p.getCode();
        if (p.hasMessage()) message = p.getMessage();
    }

    public static ResumeWfRunReply fromProto(ResumeWfRunReplyPbOrBuilder p) {
        ResumeWfRunReply out = new ResumeWfRunReply();
        out.initFrom(p);
        return out;
    }
}
