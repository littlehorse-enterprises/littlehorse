package io.littlehorse.common.model.wfrun.usertaskevent;

import com.google.protobuf.Message;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.model.objectId.TaskRunId;
import io.littlehorse.jlib.common.proto.UserTaskEventPb.UTETaskExecutedPb;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UTETaskExecuted extends LHSerializable<UTETaskExecutedPb> {

    private TaskRunId taskRunId;

    public Class<UTETaskExecutedPb> getProtoBaseClass() {
        return UTETaskExecutedPb.class;
    }

    public UTETaskExecutedPb.Builder toProto() {
        UTETaskExecutedPb.Builder out = UTETaskExecutedPb.newBuilder();
        out.setTaskRun(taskRunId.toProto());
        return out;
    }

    public void initFrom(Message proto) {
        UTETaskExecutedPb p = (UTETaskExecutedPb) proto;
        taskRunId = LHSerializable.fromProto(p.getTaskRun(), TaskRunId.class);
    }
}
