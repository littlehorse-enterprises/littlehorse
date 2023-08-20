package io.littlehorse.common.model.wfrun.taskrun;

import com.google.protobuf.Message;
import io.littlehorse.common.LHDAO;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.model.objectId.NodeRunIdModel;
import io.littlehorse.common.model.objectId.WfSpecIdModel;
import io.littlehorse.common.model.wfrun.TaskAttemptModel;
import io.littlehorse.common.model.wfrun.UserTaskRunModel;
import io.littlehorse.sdk.common.proto.UserTaskTriggerReference;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserTaskTriggerReferenceModel extends TaskRunSubSource<UserTaskTriggerReference> {

    private NodeRunIdModel nodeRunId;
    private int userTaskEventNumber;
    private WfSpecIdModel wfSpecId;

    public UserTaskTriggerReferenceModel() {}

    public UserTaskTriggerReferenceModel(UserTaskRunModel utr) {
        nodeRunId = utr.getNodeRunId();
        // Trust in the Force
        userTaskEventNumber = utr.getEvents().size();
        wfSpecId = utr.getNodeRun().getWfSpecId();
    }

    public Class<UserTaskTriggerReference> getProtoBaseClass() {
        return UserTaskTriggerReference.class;
    }

    public UserTaskTriggerReference.Builder toProto() {
        UserTaskTriggerReference.Builder out = UserTaskTriggerReference.newBuilder()
                .setWfSpecId(wfSpecId.toProto())
                .setNodeRunId(nodeRunId.toProto())
                .setUserTaskEventNumber(userTaskEventNumber);

        return out;
    }

    public void initFrom(Message proto) {
        UserTaskTriggerReference p = (UserTaskTriggerReference) proto;
        nodeRunId = LHSerializable.fromProto(p.getNodeRunId(), NodeRunIdModel.class);
        wfSpecId = LHSerializable.fromProto(p.getWfSpecId(), WfSpecIdModel.class);
        userTaskEventNumber = p.getUserTaskEventNumber();
    }

    public void onCompleted(TaskAttemptModel successfullAttempt, LHDAO dao) {
        // For now, we only "fire-and-forget" User Task Triggered Action TaskRun's, so
        // we don't actually care about what happens here.
    }

    public void onFailed(TaskAttemptModel lastFailure, LHDAO dao) {
        // Same here, we don't yet care about what happens.
    }
}
