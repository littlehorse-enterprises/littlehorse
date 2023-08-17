package io.littlehorse.common.model.command.subcommandresponse;

import com.google.protobuf.Message;
import io.littlehorse.common.model.command.AbstractResponse;
import io.littlehorse.sdk.common.proto.LHResponseCode;
import io.littlehorse.sdk.common.proto.ReportTaskResponse;

public class ReportTaskReply extends AbstractResponse<ReportTaskResponse> {

    public Class<ReportTaskResponse> getProtoBaseClass() {
        return ReportTaskResponse.class;
    }

    public ReportTaskReply() {}

    public ReportTaskReply(LHResponseCode code, String message) {
        this.code = code;
        this.message = message;
    }

    public void initFrom(Message proto) {
        ReportTaskResponse p = (ReportTaskResponse) proto;
        code = p.getCode();
        if (p.hasMessage()) {
            message = p.getMessage();
        }
    }

    public ReportTaskResponse.Builder toProto() {
        ReportTaskResponse.Builder out = ReportTaskResponse.newBuilder();
        out.setCode(code);
        if (message != null) out.setMessage(message);

        return out;
    }
}
