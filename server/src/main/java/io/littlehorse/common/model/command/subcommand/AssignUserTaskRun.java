package io.littlehorse.common.model.command.subcommand;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.LHDAO;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.model.command.SubCommand;
import io.littlehorse.common.model.command.subcommandresponse.AssignUserTaskRunReply;
import io.littlehorse.common.model.objectId.UserTaskRunId;
import io.littlehorse.common.model.wfrun.Group;
import io.littlehorse.common.model.wfrun.User;
import io.littlehorse.common.model.wfrun.UserTaskRun;
import io.littlehorse.common.model.wfrun.WfRun;
import io.littlehorse.sdk.common.proto.AssignUserTaskRunPb;
import io.littlehorse.sdk.common.proto.AssignUserTaskRunPb.AssigneeCase;
import io.littlehorse.sdk.common.proto.LHResponseCodePb;
import io.littlehorse.sdk.common.proto.UserTaskRunStatusPb;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Setter
public class AssignUserTaskRun extends SubCommand<AssignUserTaskRunPb> {

    private UserTaskRunId userTaskRunId;
    private boolean overrideClaim;

    private AssigneeCase assigneeType;
    private User user;
    private Group group;

    public Class<AssignUserTaskRunPb> getProtoBaseClass() {
        return AssignUserTaskRunPb.class;
    }

    public AssignUserTaskRunPb.Builder toProto() {
        AssignUserTaskRunPb.Builder out = AssignUserTaskRunPb
            .newBuilder()
            .setUserTaskRunId(userTaskRunId.toProto())
            .setOverrideClaim(overrideClaim);

        switch (assigneeType) {
            case USER:
                out.setUser(user.toProto());
                break;
            case GROUP:
                out.setGroup(group.toProto());
                break;
            case ASSIGNEE_NOT_SET:
                log.warn(
                    "assignee not set. Should this be LHSerdeError or validation error?"
                );
                break;
        }
        return out;
    }

    public void initFrom(Message proto) {
        AssignUserTaskRunPb p = (AssignUserTaskRunPb) proto;
        userTaskRunId =
            LHSerializable.fromProto(p.getUserTaskRunId(), UserTaskRunId.class);
        assigneeType = p.getAssigneeCase();
        overrideClaim = p.getOverrideClaim();

        switch (assigneeType) {
            case USER:
                user = LHSerializable.fromProto(p.getUser(), User.class);
                break;
            case GROUP:
                group = LHSerializable.fromProto(p.getGroup(), Group.class);
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
            out.code = LHResponseCodePb.BAD_REQUEST_ERROR;
            out.message = "Must set either userGroup or userId!";
            return out;
        }

        UserTaskRun utr = dao.getUserTaskRun(userTaskRunId);
        if (utr == null) {
            out.code = LHResponseCodePb.BAD_REQUEST_ERROR;
            out.message = "Couldn't find userTaskRun " + userTaskRunId;
            return out;
        }

        if (!overrideClaim && utr.getUser() != null) {
            out.code = LHResponseCodePb.ALREADY_EXISTS_ERROR;
            out.message = "User Task Run already assigned to " + utr.getUser();
            return out;
        }

        if (
            utr.getStatus() != UserTaskRunStatusPb.ASSIGNED &&
            utr.getStatus() != UserTaskRunStatusPb.UNASSIGNED
        ) {
            out.code = LHResponseCodePb.BAD_REQUEST_ERROR;
            out.message =
                "Couldn't reassign User Task Run since it  is in terminal status " +
                utr.getStatus();
        }

        // In the future, we could add some verification to make sure that the
        // user actually exists. For now, this is fine.
        utr.reassignTo(this);
        WfRun wfRun = dao.getWfRun(getWfRunId());
        if (wfRun == null) {
            log.error(
                "Impossible: Got the UserTaskRun but WfRun missing {}",
                getWfRunId()
            );
            return out;
        }

        wfRun.advance(new Date());

        out.code = LHResponseCodePb.OK;
        return out;
    }

    public boolean hasResponse() {
        return true;
    }

    public String getPartitionKey() {
        return getWfRunId();
    }
}
