package io.littlehorse.common.model.wfrun.taskrun;

import com.google.protobuf.Message;
import io.littlehorse.common.LHDAO;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.model.objectId.NodeRunId;
import io.littlehorse.common.model.objectId.WfSpecId;
import io.littlehorse.common.model.wfrun.TaskAttempt;
import io.littlehorse.common.model.wfrun.subnoderun.UserTaskRun;
import io.littlehorse.jlib.common.proto.UserTaskTriggerReferencePb;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserTaskTriggerReference
    extends TaskRunSubSource<UserTaskTriggerReferencePb> {

    private NodeRunId nodeRunId;
    private int userTaskEventNumber;
    private WfSpecId wfSpecId;

    public UserTaskTriggerReference() {}

    public UserTaskTriggerReference(UserTaskRun utr) {
        nodeRunId = utr.getNodeRun().getObjectId();
        // Trust in the Force
        userTaskEventNumber = utr.getEvents().size();
        wfSpecId = utr.getNodeRun().getWfSpecId();
    }

    public Class<UserTaskTriggerReferencePb> getProtoBaseClass() {
        return UserTaskTriggerReferencePb.class;
    }

    public UserTaskTriggerReferencePb.Builder toProto() {
        UserTaskTriggerReferencePb.Builder out = UserTaskTriggerReferencePb
            .newBuilder()
            .setWfSpecId(wfSpecId.toProto())
            .setNodeRunId(nodeRunId.toProto())
            .setUserTaskEventNumber(userTaskEventNumber);

        return out;
    }

    public void initFrom(Message proto) {
        UserTaskTriggerReferencePb p = (UserTaskTriggerReferencePb) proto;
        nodeRunId = LHSerializable.fromProto(p.getNodeRunId(), NodeRunId.class);
        wfSpecId = LHSerializable.fromProto(p.getWfSpecId(), WfSpecId.class);
        userTaskEventNumber = p.getUserTaskEventNumber();
    }

    public void onCompleted(TaskAttempt successfullAttempt, LHDAO dao) {
        // For now, we only "fire-and-forget" User Task Triggered Action TaskRun's, so
        // we don't actually care about what happens here.
    }

    public void onFailed(TaskAttempt lastFailure, LHDAO dao) {
        // Same here, we don't yet care about what happens.
    }
}
