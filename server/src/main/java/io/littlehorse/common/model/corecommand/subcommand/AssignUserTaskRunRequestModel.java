package io.littlehorse.common.model.corecommand.subcommand;

import com.google.protobuf.Empty;
import com.google.protobuf.Message;
import io.grpc.Status;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.dao.CoreProcessorDAO;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.corecommand.SubCommand;
import io.littlehorse.common.model.getable.core.usertaskrun.UserGroupModel;
import io.littlehorse.common.model.getable.core.usertaskrun.UserModel;
import io.littlehorse.common.model.getable.core.usertaskrun.UserTaskRunModel;
import io.littlehorse.common.model.getable.core.wfrun.WfRunModel;
import io.littlehorse.common.model.getable.objectId.UserTaskRunIdModel;
import io.littlehorse.sdk.common.proto.AssignUserTaskRunRequest;
import io.littlehorse.sdk.common.proto.AssignUserTaskRunRequest.AssigneeCase;
import io.littlehorse.sdk.common.proto.UserTaskRunStatus;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Setter
public class AssignUserTaskRunRequestModel extends SubCommand<AssignUserTaskRunRequest> {

    private UserTaskRunIdModel userTaskRunId;
    private boolean overrideClaim;

    private AssigneeCase assigneeType;
    private UserModel user;
    private UserGroupModel userGroup;

    public Class<AssignUserTaskRunRequest> getProtoBaseClass() {
        return AssignUserTaskRunRequest.class;
    }

    public AssignUserTaskRunRequest.Builder toProto() {
        AssignUserTaskRunRequest.Builder out = AssignUserTaskRunRequest.newBuilder()
                .setUserTaskRunId(userTaskRunId.toProto())
                .setOverrideClaim(overrideClaim);
        switch (assigneeType) {
            case USER:
                out.setUser(user.toProto());
                break;
            case USER_GROUP:
                out.setUserGroup(userGroup.toProto());
                break;
            case ASSIGNEE_NOT_SET:
                log.warn("assignee not set. Should this be LHSerdeError or validation error?");
                break;
        }
        return out;
    }

    public void initFrom(Message proto) {
        AssignUserTaskRunRequest p = (AssignUserTaskRunRequest) proto;
        userTaskRunId = LHSerializable.fromProto(p.getUserTaskRunId(), UserTaskRunIdModel.class);
        assigneeType = p.getAssigneeCase();
        overrideClaim = p.getOverrideClaim();

        switch (assigneeType) {
            case USER:
                user = LHSerializable.fromProto(p.getUser(), UserModel.class);
                break;
            case USER_GROUP:
                userGroup = LHSerializable.fromProto(p.getUserGroup(), UserGroupModel.class);
                break;
            case ASSIGNEE_NOT_SET:
                log.warn("Unset assignee. Should this be error?");
                break;
        }
    }

    public String getWfRunId() {
        return userTaskRunId.getWfRunId();
    }

    public Empty process(CoreProcessorDAO dao, LHConfig config) {

        if (assigneeType == AssigneeCase.ASSIGNEE_NOT_SET) {
            throw new LHApiException(Status.INVALID_ARGUMENT, "Must set either userGroup or userId!");
        }

        UserTaskRunModel utr = dao.get(userTaskRunId);
        if (utr == null) {
            throw new LHApiException(Status.NOT_FOUND, "Couldn't find UserTaskRun " + userTaskRunId);
        }

        if (!overrideClaim && utr.getUser() != null) {
            throw new LHApiException(Status.FAILED_PRECONDITION, "User Task Run already assigned to " + utr.getUser());
        }

        if (utr.getStatus() != UserTaskRunStatus.ASSIGNED && utr.getStatus() != UserTaskRunStatus.UNASSIGNED) {
            throw new LHApiException(
                    Status.FAILED_PRECONDITION,
                    "Couldn't reassign User Task Run since it  is in terminal status " + utr.getStatus());
        }

        // In the future, we could add some verification to make sure that the
        // user actually exists. For now, this is fine.
        utr.reassignTo(this);
        WfRunModel wfRunModel = dao.getWfRun(getWfRunId());
        if (wfRunModel == null) {
            throw new LHApiException(Status.DATA_LOSS, "Impossible: got UserTaskRun but missing WfRun");
        }

        wfRunModel.advance(new Date());

        return Empty.getDefaultInstance();
    }

    public boolean hasResponse() {
        return true;
    }

    public String getPartitionKey() {
        return getWfRunId();
    }
}
