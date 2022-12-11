package io.littlehorse.common.model.command.subcommandresponse;

import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.model.command.AbstractResponse;
import io.littlehorse.common.model.wfrun.TaskScheduleRequest;
import io.littlehorse.common.proto.PollTaskReplyPb;
import io.littlehorse.common.proto.PollTaskReplyPbOrBuilder;

public class TaskClaimReply extends AbstractResponse<PollTaskReplyPb> {

    public TaskScheduleRequest result;

    public Class<PollTaskReplyPb> getProtoBaseClass() {
        return PollTaskReplyPb.class;
    }

    public PollTaskReplyPb.Builder toProto() {
        PollTaskReplyPb.Builder out = PollTaskReplyPb.newBuilder();
        out.setCode(code);
        if (result != null) out.setResult(result.toProto());
        if (message != null) out.setMessage(message);
        return out;
    }

    public void initFrom(MessageOrBuilder proto) {
        PollTaskReplyPbOrBuilder p = (PollTaskReplyPbOrBuilder) proto;
        if (p.hasResult()) {
            result = TaskScheduleRequest.fromProto(p.getResultOrBuilder());
        }
        code = p.getCode();
        if (p.hasMessage()) message = p.getMessage();
    }
}
