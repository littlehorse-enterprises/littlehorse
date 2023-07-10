package io.littlehorse.common.model.command.subcommandresponse;

import com.google.protobuf.Message;
import io.littlehorse.common.model.command.AbstractResponse;
import io.littlehorse.sdk.common.proto.LHResponseCodePb;
import io.littlehorse.sdk.common.proto.ReportTaskReplyPb;

public class ReportTaskReply extends AbstractResponse<ReportTaskReplyPb> {

    public Class<ReportTaskReplyPb> getProtoBaseClass() {
        return ReportTaskReplyPb.class;
    }

    public ReportTaskReply() {}

    public ReportTaskReply(LHResponseCodePb code, String message) {
        this.code = code;
        this.message = message;
    }

    public void initFrom(Message proto) {
        ReportTaskReplyPb p = (ReportTaskReplyPb) proto;
        code = p.getCode();
        if (p.hasMessage()) {
            message = p.getMessage();
        }
    }

    public ReportTaskReplyPb.Builder toProto() {
        ReportTaskReplyPb.Builder out = ReportTaskReplyPb.newBuilder();
        out.setCode(code);
        if (message != null) out.setMessage(message);

        return out;
    }
}
