package io.littlehorse.common.model.wfrun.usertaskevent;

import com.google.protobuf.Message;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.model.objectId.TaskRunIdModel;
import io.littlehorse.sdk.common.proto.UserTaskEvent.UTETaskExecuted;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UTETaskExecutedModel extends LHSerializable<UTETaskExecuted> {

    private TaskRunIdModel taskRunId;

    public UTETaskExecutedModel() {}

    public UTETaskExecutedModel(TaskRunIdModel taskRunId) {
        this.taskRunId = taskRunId;
    }

    public Class<UTETaskExecuted> getProtoBaseClass() {
        return UTETaskExecuted.class;
    }

    public UTETaskExecuted.Builder toProto() {
        UTETaskExecuted.Builder out = UTETaskExecuted.newBuilder();
        out.setTaskRun(taskRunId.toProto());
        return out;
    }

    public void initFrom(Message proto) {
        UTETaskExecuted p = (UTETaskExecuted) proto;
        taskRunId = LHSerializable.fromProto(p.getTaskRun(), TaskRunIdModel.class);
    }
}
