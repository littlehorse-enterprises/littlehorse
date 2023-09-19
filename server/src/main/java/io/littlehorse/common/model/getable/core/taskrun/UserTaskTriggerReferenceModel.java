package io.littlehorse.common.model.getable.core.taskrun;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.dao.CoreProcessorDAO;
import io.littlehorse.common.model.getable.core.usertaskrun.UserTaskRunModel;
import io.littlehorse.common.model.getable.objectId.NodeRunIdModel;
import io.littlehorse.common.model.getable.objectId.WfSpecIdModel;
import io.littlehorse.sdk.common.proto.UserTaskTriggerReference;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserTaskTriggerReferenceModel extends TaskRunSubSource<UserTaskTriggerReference> {

    private NodeRunIdModel nodeRunId;
    private int userTaskEventNumber;
    private WfSpecIdModel wfSpecId;

    private String userId;
    private String userGroup;

    public UserTaskTriggerReferenceModel() {}

    public UserTaskTriggerReferenceModel(UserTaskRunModel utr) {
        nodeRunId = utr.getNodeRunId();
        // Trust in the Force
        userTaskEventNumber = utr.getEvents().size();
        wfSpecId = utr.getNodeRun().getWfSpecId();

        this.userId = utr.getUserId();
        this.userGroup = utr.getUserGroup();
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

    public void onCompleted(TaskAttemptModel successfullAttempt, CoreProcessorDAO dao) {
        // For now, we only "fire-and-forget" User Task Triggered Action TaskRun's, so
        // we don't actually care about what happens here.
    }

    public void onFailed(TaskAttemptModel lastFailure, CoreProcessorDAO dao) {
        // Same here, we don't yet care about what happens.
    }
}
