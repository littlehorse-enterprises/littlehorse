package io.littlehorse.common.model.getable.core.taskrun;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.getable.core.usertaskrun.UserTaskRunModel;
import io.littlehorse.common.model.getable.objectId.NodeRunIdModel;
import io.littlehorse.sdk.common.proto.UserTaskTriggerReference;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.topology.core.ProcessorExecutionContext;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserTaskTriggerReferenceModel extends TaskRunSubSource<UserTaskTriggerReference> {

    private NodeRunIdModel nodeRunId;
    private int userTaskEventNumber;

    private String userId;
    private String userGroup;

    public UserTaskTriggerReferenceModel() {}

    public UserTaskTriggerReferenceModel(UserTaskRunModel utr, ProcessorExecutionContext processorContext) {
        nodeRunId = utr.getNodeRunId();
        // Trust in the Force
        userTaskEventNumber = utr.getEvents().size();

        this.userId = utr.getUserId();
        this.userGroup = utr.getUserGroup();
    }

    public Class<UserTaskTriggerReference> getProtoBaseClass() {
        return UserTaskTriggerReference.class;
    }

    public UserTaskTriggerReference.Builder toProto() {
        UserTaskTriggerReference.Builder out = UserTaskTriggerReference.newBuilder()
                .setNodeRunId(nodeRunId.toProto())
                .setUserTaskEventNumber(userTaskEventNumber);

        return out;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        UserTaskTriggerReference p = (UserTaskTriggerReference) proto;
        nodeRunId = LHSerializable.fromProto(p.getNodeRunId(), NodeRunIdModel.class, context);
        userTaskEventNumber = p.getUserTaskEventNumber();
    }

    @Override
    public void onCompleted(TaskAttemptModel successfullAttempt) {
        // For now, we only "fire-and-forget" User Task Triggered Action TaskRun's, so
        // we don't actually care about what happens here.
    }

    @Override
    public void onFailed(TaskAttemptModel lastFailure) {
        // Same here, we don't yet care about what happens.
    }
}
