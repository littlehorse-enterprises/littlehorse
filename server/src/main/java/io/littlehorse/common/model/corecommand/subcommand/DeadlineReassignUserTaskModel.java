package io.littlehorse.common.model.corecommand.subcommand;

import com.google.protobuf.Empty;
import com.google.protobuf.Message;
import io.grpc.Status;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.dao.CoreProcessorDAO;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.corecommand.CoreSubCommand;
import io.littlehorse.common.model.getable.core.usertaskrun.UserTaskRunModel;
import io.littlehorse.common.model.getable.global.wfspec.variable.VariableAssignmentModel;
import io.littlehorse.common.model.getable.objectId.UserTaskRunIdModel;
import io.littlehorse.common.proto.DeadlineReassignUserTask;
import io.littlehorse.sdk.common.exception.LHSerdeError;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeadlineReassignUserTaskModel extends CoreSubCommand<DeadlineReassignUserTask> {

    private UserTaskRunIdModel source;
    private VariableAssignmentModel newUserId;
    private VariableAssignmentModel newUserGroup;

    public DeadlineReassignUserTaskModel() {}

    public DeadlineReassignUserTaskModel(
            UserTaskRunIdModel source, VariableAssignmentModel newUserId, VariableAssignmentModel newUserGroup) {
        this.source = source;
        this.newUserId = newUserId;
        this.newUserGroup = newUserGroup;
    }

    @Override
    public DeadlineReassignUserTask.Builder toProto() {
        DeadlineReassignUserTask.Builder builder =
                DeadlineReassignUserTask.newBuilder().setUserTask(source.toProto());
        if (newUserId != null) builder.setNewUserId(newUserId.toProto());
        if (newUserGroup != null) builder.setNewUserGroup(newUserGroup.toProto());

        return builder;
    }

    @Override
    public void initFrom(Message proto) throws LHSerdeError {
        DeadlineReassignUserTask p = (DeadlineReassignUserTask) proto;
        if (p.hasNewUserId()) newUserId = VariableAssignmentModel.fromProto(p.getNewUserId());
        if (p.hasNewUserGroup()) newUserGroup = VariableAssignmentModel.fromProto(p.getNewUserGroup());
        source = LHSerializable.fromProto(p.getUserTask(), UserTaskRunIdModel.class);
    }

    @Override
    public Class<DeadlineReassignUserTask> getProtoBaseClass() {
        return DeadlineReassignUserTask.class;
    }

    @Override
    public Empty process(CoreProcessorDAO dao, LHServerConfig config) {
        UserTaskRunModel userTaskRun = dao.get(source);
        if (userTaskRun == null) {
            throw new LHApiException(Status.INVALID_ARGUMENT, "Specified NodeRun not a UserTaskRun");
        }

        userTaskRun.deadlineReassign(this);
        return Empty.getDefaultInstance();
    }

    @Override
    public boolean hasResponse() {
        // Reassigned User Task are sent by the LHTimer infrastructure, which means
        // there is no actual client waiting for the response.
        return false;
    }

    @Override
    public String getPartitionKey() {
        return this.source.getPartitionKey().get();
    }
}
