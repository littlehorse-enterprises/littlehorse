package io.littlehorse.common.model.command.subcommandresponse;

import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.proto.LHResponseCodePb;
import io.littlehorse.common.proto.RunWfReplyPb;
import io.littlehorse.common.proto.RunWfReplyPbOrBuilder;

public class RunWfReply extends LHSerializable<RunWfReplyPb> {

    public LHResponseCodePb code;
    public String message;
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

    public void initFrom(MessageOrBuilder proto) {
        RunWfReplyPbOrBuilder p = (RunWfReplyPbOrBuilder) proto;
        code = p.getCode();
        if (p.hasMessage()) message = p.getMessage();
        if (p.hasWfSpecVersion()) wfSpecVersion = p.getWfSpecVersion();
        if (p.hasWfRunId()) wfRunId = p.getWfRunId();
    }
}
