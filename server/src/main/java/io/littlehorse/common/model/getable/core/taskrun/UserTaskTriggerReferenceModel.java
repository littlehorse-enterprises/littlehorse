package io.littlehorse.common.model.getable.core.taskrun;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.getable.core.usertaskrun.UserTaskRunModel;
import io.littlehorse.common.model.getable.objectId.NodeRunIdModel;
import io.littlehorse.common.model.getable.objectId.WfRunIdModel;
import io.littlehorse.sdk.common.proto.UserTaskTriggerReference;
import io.littlehorse.server.streams.topology.core.CoreProcessorContext;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserTaskTriggerReferenceModel extends LHSerializable<UserTaskTriggerReference>
        implements TaskRunSubSource {

    private NodeRunIdModel nodeRunId;
    private int userTaskEventNumber;

    private String userId;
    private String userGroup;

    public UserTaskTriggerReferenceModel() {}

    public UserTaskTriggerReferenceModel(UserTaskRunModel utr, CoreProcessorContext processorContext) {
        nodeRunId = utr.getNodeRunId();
        // Trust in the Force
        userTaskEventNumber = utr.getEvents().size();

        this.userId = utr.getUserId();
        this.userGroup = utr.getUserGroup();
    }

    @Override
    public Class<UserTaskTriggerReference> getProtoBaseClass() {
        return UserTaskTriggerReference.class;
    }

    @Override
    public UserTaskTriggerReference.Builder toProto() {
        UserTaskTriggerReference.Builder out = UserTaskTriggerReference.newBuilder()
                .setNodeRunId(nodeRunId.toProto())
                .setUserTaskEventNumber(userTaskEventNumber);

        if (userId != null) {
            out.setUserId(this.userId);
        }

        if (userGroup != null) {
            out.setUserGroup(this.userGroup);
        }

        return out;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        UserTaskTriggerReference p = (UserTaskTriggerReference) proto;
        nodeRunId = LHSerializable.fromProto(p.getNodeRunId(), NodeRunIdModel.class, context);
        userTaskEventNumber = p.getUserTaskEventNumber();

        if (p.hasUserId()) {
            userId = p.getUserId();
        }

        if (p.hasUserGroup()) {
            userGroup = p.getUserGroup();
        }
    }

    @Override
    public WfRunIdModel getWfRunId() {
        return nodeRunId.getWfRunId();
    }
}
