package io.littlehorse.common.model.command.subcommandresponse;

import com.google.protobuf.Message;
import io.littlehorse.common.model.command.AbstractResponse;
import io.littlehorse.common.model.wfrun.ScheduledTaskModel;
import io.littlehorse.sdk.common.proto.PollTaskResponse;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskClaimReply extends AbstractResponse<PollTaskResponse> {

    public ScheduledTaskModel result;

    public Class<PollTaskResponse> getProtoBaseClass() {
        return PollTaskResponse.class;
    }

    public PollTaskResponse.Builder toProto() {
        PollTaskResponse.Builder out = PollTaskResponse.newBuilder();
        out.setCode(code);
        if (result != null) out.setResult(result.toProto());
        if (message != null) out.setMessage(message);
        return out;
    }

    public void initFrom(Message proto) {
        PollTaskResponse p = (PollTaskResponse) proto;
        if (p.hasResult()) {
            result = ScheduledTaskModel.fromProto(p.getResult());
        }
        code = p.getCode();
        if (p.hasMessage()) message = p.getMessage();
    }
}
