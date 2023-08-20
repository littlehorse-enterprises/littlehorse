package io.littlehorse.common.model.command.subcommandresponse;

import com.google.protobuf.Message;
import io.littlehorse.common.model.command.AbstractResponse;
import io.littlehorse.sdk.common.proto.ResumeWfRunResponse;

public class ResumeWfRunReply extends AbstractResponse<ResumeWfRunResponse> {

    public Class<ResumeWfRunResponse> getProtoBaseClass() {
        return ResumeWfRunResponse.class;
    }

    public ResumeWfRunResponse.Builder toProto() {
        ResumeWfRunResponse.Builder out = ResumeWfRunResponse.newBuilder();
        out.setCode(code);
        if (message != null) {
            out.setMessage(message);
        }
        return out;
    }

    public void initFrom(Message proto) {
        ResumeWfRunResponse p = (ResumeWfRunResponse) proto;
        code = p.getCode();
        if (p.hasMessage()) message = p.getMessage();
    }

    public static ResumeWfRunReply fromProto(ResumeWfRunResponse p) {
        ResumeWfRunReply out = new ResumeWfRunReply();
        out.initFrom(p);
        return out;
    }
}
