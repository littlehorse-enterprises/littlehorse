package io.littlehorse.common.model.command.subcommandresponse;

import com.google.protobuf.Message;
import io.littlehorse.common.model.command.AbstractResponse;
import io.littlehorse.jlib.common.proto.ReportTaskReplyPb;

public class ReportTaskReply extends AbstractResponse<ReportTaskReplyPb> {

    public Class<ReportTaskReplyPb> getProtoBaseClass() {
        return ReportTaskReplyPb.class;
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
