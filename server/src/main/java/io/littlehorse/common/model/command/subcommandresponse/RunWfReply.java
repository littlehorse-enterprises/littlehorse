package io.littlehorse.common.model.command.subcommandresponse;

import com.google.protobuf.Message;
import io.littlehorse.common.model.command.AbstractResponse;
import io.littlehorse.sdk.common.proto.RunWfResponse;

public class RunWfReply extends AbstractResponse<RunWfResponse> {

    public Integer wfSpecVersion;
    public String wfRunId;

    public Class<RunWfResponse> getProtoBaseClass() {
        return RunWfResponse.class;
    }

    public RunWfResponse.Builder toProto() {
        RunWfResponse.Builder out = RunWfResponse.newBuilder().setCode(code);
        if (message != null) out.setMessage(message);
        if (wfSpecVersion != null) out.setWfSpecVersion(wfSpecVersion);
        if (wfRunId != null) out.setWfRunId(wfRunId);
        return out;
    }

    public void initFrom(Message proto) {
        RunWfResponse p = (RunWfResponse) proto;
        code = p.getCode();
        if (p.hasMessage()) message = p.getMessage();
        if (p.hasWfSpecVersion()) wfSpecVersion = p.getWfSpecVersion();
        if (p.hasWfRunId()) wfRunId = p.getWfRunId();
    }
}
