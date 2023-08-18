package io.littlehorse.common.model.command.subcommand;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.LHDAO;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.model.command.AbstractResponse;
import io.littlehorse.common.model.command.SubCommand;
import io.littlehorse.common.model.objectId.NodeRunIdModel;
import io.littlehorse.common.model.wfrun.NodeRunModel;
import io.littlehorse.common.model.wfrun.UserTaskRunModel;
import io.littlehorse.common.proto.ReassignedUserTaskPb;
import io.littlehorse.sdk.common.exception.LHSerdeError;
import io.littlehorse.sdk.common.proto.UserTaskRunStatus;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ReassignUserTask extends SubCommand<ReassignedUserTaskPb> {

    private NodeRunIdModel source;
    private String newOwner;
    private ReassignedUserTaskPb.AssignToCase assignToCase;

    public ReassignUserTask() {}

    public ReassignUserTask(NodeRunIdModel source, String newOwner, ReassignedUserTaskPb.AssignToCase assignToCase) {
        this.source = source;
        this.newOwner = newOwner;
        this.assignToCase = assignToCase;
    }

    @Override
    public ReassignedUserTaskPb.Builder toProto() {
        ReassignedUserTaskPb.Builder builder = ReassignedUserTaskPb.newBuilder();
        if (assignToCase == ReassignedUserTaskPb.AssignToCase.USER_ID) {
            builder.setUserId(newOwner);
        } else {
            builder.setUserGroup(newOwner);
        }
        builder.setSource(source.toProto());
        return builder;
    }

    @Override
    public void initFrom(Message proto) throws LHSerdeError {
        ReassignedUserTaskPb p = (ReassignedUserTaskPb) proto;
        assignToCase = p.getAssignToCase();
        if (p.getAssignToCase() == ReassignedUserTaskPb.AssignToCase.USER_ID) {
            newOwner = p.getUserId();
        } else {
            newOwner = p.getUserGroup();
        }
        source = LHSerializable.fromProto(p.getSource(), NodeRunIdModel.class);
    }

    @Override
    public Class<ReassignedUserTaskPb> getProtoBaseClass() {
        return ReassignedUserTaskPb.class;
    }

    @Override
    public AbstractResponse<?> process(LHDAO dao, LHConfig config) {
        NodeRunModel nodeRunModel = dao.getNodeRun(source);
        UserTaskRunModel userTaskRun =
                dao.getUserTaskRun(nodeRunModel.getUserTaskRun().getUserTaskRunId());
        if (userTaskRun.getStatus() == UserTaskRunStatus.ASSIGNED) {
            userTaskRun.deadlineReassign(newOwner, assignToCase);
        }
        return null;
    }

    @Override
    public boolean hasResponse() {
        // Reassigned User Task are sent by the LHTimer infrastructure, which means
        // there is no actual client waiting for the response.
        return false;
    }

    @Override
    public String getPartitionKey() {
        return this.source.getPartitionKey();
    }
}
