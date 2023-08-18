package io.littlehorse.common.model.command.subcommand;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.LHDAO;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.model.command.SubCommand;
import io.littlehorse.common.model.command.subcommandresponse.AssignUserTaskRunReply;
import io.littlehorse.common.model.objectId.UserTaskRunIdModel;
import io.littlehorse.common.model.wfrun.UserGroupModel;
import io.littlehorse.common.model.wfrun.UserModel;
import io.littlehorse.common.model.wfrun.UserTaskRunModel;
import io.littlehorse.common.model.wfrun.WfRunModel;
import io.littlehorse.sdk.common.proto.AssignUserTaskRunRequest;
import io.littlehorse.sdk.common.proto.AssignUserTaskRunRequest.AssigneeCase;
import io.littlehorse.sdk.common.proto.LHResponseCode;
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
        AssignUserTaskRunRequest.Builder out =
                AssignUserTaskRunRequest.newBuilder()
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

    public AssignUserTaskRunReply process(LHDAO dao, LHConfig config) {
        AssignUserTaskRunReply out = new AssignUserTaskRunReply();

        if (assigneeType == AssigneeCase.ASSIGNEE_NOT_SET) {
            out.code = LHResponseCode.BAD_REQUEST_ERROR;
            out.message = "Must set either userGroup or userId!";
            return out;
        }

        UserTaskRunModel utr = dao.getUserTaskRun(userTaskRunId);
        if (utr == null) {
            out.code = LHResponseCode.BAD_REQUEST_ERROR;
            out.message = "Couldn't find userTaskRun " + userTaskRunId;
            return out;
        }

        if (!overrideClaim && utr.getUser() != null) {
            out.code = LHResponseCode.ALREADY_EXISTS_ERROR;
            out.message = "User Task Run already assigned to " + utr.getUser();
            return out;
        }

        if (utr.getStatus() != UserTaskRunStatus.ASSIGNED
                && utr.getStatus() != UserTaskRunStatus.UNASSIGNED) {
            out.code = LHResponseCode.BAD_REQUEST_ERROR;
            out.message =
                    "Couldn't reassign User Task Run since it  is in terminal status "
                            + utr.getStatus();
        }

        // In the future, we could add some verification to make sure that the
        // user actually exists. For now, this is fine.
        utr.reassignTo(this);
        WfRunModel wfRunModel = dao.getWfRun(getWfRunId());
        if (wfRunModel == null) {
            log.error("Impossible: Got the UserTaskRun but WfRun missing {}", getWfRunId());
            return out;
        }

        wfRunModel.advance(new Date());

        out.code = LHResponseCode.OK;
        return out;
    }

    public boolean hasResponse() {
        return true;
    }

    public String getPartitionKey() {
        return getWfRunId();
    }
}
