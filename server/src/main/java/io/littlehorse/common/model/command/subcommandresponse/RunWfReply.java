package io.littlehorse.common.model.command.subcommandresponse;

import com.google.protobuf.Message;
import io.littlehorse.common.model.command.AbstractResponse;
import io.littlehorse.sdk.common.proto.RunWfReplyPb;

public class RunWfReply extends AbstractResponse<RunWfReplyPb> {

    public Integer wfSpecVersion;
    public String wfRunId;

    public Class<RunWfReplyPb> getProtoBaseClass() {
        return RunWfReplyPb.class;
    }

    public RunWfReplyPb.Builder toProto() {
        RunWfReplyPb.Builder out = RunWfReplyPb.newBuilder().setCode(code);
        if (message != null) out.setMessage(message);
        if (wfSpecVersion != null) out.setWfSpecVersion(wfSpecVersion);
        if (wfRunId != null) out.setWfRunId(wfRunId);
        return out;
    }

    public void initFrom(Message proto) {
        RunWfReplyPb p = (RunWfReplyPb) proto;
        code = p.getCode();
        if (p.hasMessage()) message = p.getMessage();
        if (p.hasWfSpecVersion()) wfSpecVersion = p.getWfSpecVersion();
        if (p.hasWfRunId()) wfRunId = p.getWfRunId();
    }
}
