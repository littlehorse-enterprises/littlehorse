package io.littlehorse.common.model.corecommand.subcommand;

import com.google.protobuf.Empty;
import com.google.protobuf.Message;
import io.grpc.Status;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.dao.CoreProcessorDAO;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.corecommand.SubCommand;
import io.littlehorse.common.model.getable.core.noderun.NodeRunModel;
import io.littlehorse.common.model.getable.core.usertaskrun.UserTaskRunModel;
import io.littlehorse.common.model.getable.objectId.NodeRunIdModel;
import io.littlehorse.common.proto.ReassignedUserTaskPb;
import io.littlehorse.sdk.common.exception.LHSerdeError;
import io.littlehorse.sdk.common.proto.UserTaskRunStatus;

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
    public Empty process(CoreProcessorDAO dao, LHServerConfig config) {
        NodeRunModel nodeRunModel = dao.get(source);
        if (nodeRunModel == null) {
            throw new LHApiException(Status.NOT_FOUND, "Couldn't find specified noderun");
        }

        UserTaskRunModel userTaskRun = dao.get(nodeRunModel.getUserTaskRun().getUserTaskRunId());
        if (userTaskRun == null) {
            throw new LHApiException(Status.INVALID_ARGUMENT, "Specified NodeRun not a UserTaskRun");
        }

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
        return this.source.getPartitionKey().get();
    }
}
