package io.littlehorse.common.model.getable.core.taskrun;

import java.util.Optional;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.getable.core.usertaskrun.UserTaskRunModel;
import io.littlehorse.common.model.getable.objectId.NodeRunIdModel;
import io.littlehorse.common.model.getable.objectId.WfRunIdModel;
import io.littlehorse.sdk.common.proto.UserTaskTriggerReference;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.topology.core.ProcessorExecutionContext;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserTaskTriggerReferenceModel extends LHSerializable<UserTaskTriggerReference>
        implements TaskRunSubSource {

    private NodeRunIdModel nodeRunId;
    private int userTaskEventNumber;

    private Optional<String> userId;
    private Optional<String> userGroup;

    public UserTaskTriggerReferenceModel() {}

    public UserTaskTriggerReferenceModel(UserTaskRunModel utr, ProcessorExecutionContext processorContext) {
        nodeRunId = utr.getNodeRunId();
        // Trust in the Force
        userTaskEventNumber = utr.getEvents().size();

        this.userId = Optional.of(utr.getUserId());
        this.userGroup = Optional.of(utr.getUserGroup());
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

        if (userId.isPresent()) {
            out.setUserId(this.userId.get());
        }
        
        if (userGroup.isPresent()) {
            out.setUserGroup(this.userGroup.get());
        }

        return out;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        UserTaskTriggerReference p = (UserTaskTriggerReference) proto;
        nodeRunId = LHSerializable.fromProto(p.getNodeRunId(), NodeRunIdModel.class, context);
        userTaskEventNumber = p.getUserTaskEventNumber();
        userId = Optional.ofNullable(p.getUserId());
        userGroup = Optional.ofNullable(p.getUserGroup());
    }

    @Override
    public WfRunIdModel getWfRunId() {
        return nodeRunId.getWfRunId();
    }
}
