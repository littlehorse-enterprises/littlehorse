package io.littlehorse.common.model.command.subcommand;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.LHDAO;
import io.littlehorse.common.model.command.SubCommand;
import io.littlehorse.common.model.command.subcommandresponse.AssignUserTaskRunReply;
import io.littlehorse.common.model.meta.WfSpec;
import io.littlehorse.common.model.wfrun.NodeRun;
import io.littlehorse.common.model.wfrun.ThreadRun;
import io.littlehorse.common.model.wfrun.WfRun;
import io.littlehorse.common.model.wfrun.subnoderun.UserTaskRun;
import io.littlehorse.sdk.common.proto.AssignUserTaskRunPb;
import io.littlehorse.sdk.common.proto.AssignUserTaskRunPb.AssigneeCase;
import io.littlehorse.sdk.common.proto.LHResponseCodePb;
import io.littlehorse.sdk.common.proto.NodeRunPb.NodeTypeCase;
import io.littlehorse.sdk.common.proto.UserTaskRunStatusPb;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AssignUserTaskRun extends SubCommand<AssignUserTaskRunPb> {

    public String wfRunId;
    public int threadRunNumber;
    public int nodeRunPosition;

    public boolean overrideClaim;

    public AssigneeCase assigneeType;
    public String userId;
    public String userGroup;

    public Class<AssignUserTaskRunPb> getProtoBaseClass() {
        return AssignUserTaskRunPb.class;
    }

    public AssignUserTaskRunPb.Builder toProto() {
        AssignUserTaskRunPb.Builder out = AssignUserTaskRunPb
            .newBuilder()
            .setWfRunId(wfRunId)
            .setThreadRunNumber(threadRunNumber)
            .setNodeRunPosition(nodeRunPosition)
            .setOverrideClaim(overrideClaim);

        switch (assigneeType) {
            case USER_ID:
                out.setUserId(userId);
                break;
            case USER_GROUP:
                out.setUserGroup(userGroup);
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
        wfRunId = p.getWfRunId();
        threadRunNumber = p.getThreadRunNumber();
        nodeRunPosition = p.getNodeRunPosition();
        assigneeType = p.getAssigneeCase();
        overrideClaim = p.getOverrideClaim();

        switch (assigneeType) {
            case USER_ID:
                userId = p.getUserId();
                break;
            case USER_GROUP:
                userGroup = p.getUserGroup();
                break;
            case ASSIGNEE_NOT_SET:
                log.warn("Unset assignee. Should this be error?");
                break;
        }
    }

    public AssignUserTaskRunReply process(LHDAO dao, LHConfig config) {
        WfRun wfRun = dao.getWfRun(wfRunId);
        AssignUserTaskRunReply out = new AssignUserTaskRunReply();

        if (assigneeType == AssigneeCase.ASSIGNEE_NOT_SET) {
            out.code = LHResponseCodePb.BAD_REQUEST_ERROR;
            out.message = "Must set either userGroup or userId!";
            return out;
        }

        if (wfRun == null) {
            out.code = LHResponseCodePb.BAD_REQUEST_ERROR;
            out.message = "Provided invalid wfRunId";
            return out;
        }

        // First, find the WfSpec
        WfSpec wfSpec = dao.getWfSpec(wfRun.wfSpecName, wfRun.wfSpecVersion);
        if (wfSpec == null) {
            wfRun.failDueToWfSpecDeletion();
            out.code = LHResponseCodePb.NOT_FOUND_ERROR;
            out.message = "Apparently WfSpec was deleted!";
            return out;
        }

        wfRun.wfSpec = wfSpec;

        // Next, process the node.
        ThreadRun thread = wfRun.threadRuns.get(threadRunNumber);
        if (thread == null) {
            out.code = LHResponseCodePb.BAD_REQUEST_ERROR;
            out.message = "Could not find specified threadRun";
            return out;
        }

        NodeRun nr = thread.getNodeRun(this.nodeRunPosition);
        if (nr == null) {
            out.code = LHResponseCodePb.BAD_REQUEST_ERROR;
            out.message = "Could not find specified nodeRun";
            return out;
        }
        if (nr.type != NodeTypeCase.USER_TASK) {
            out.code = LHResponseCodePb.BAD_REQUEST_ERROR;
            out.message = "Specified NodeRun not a User Task Node!";
            return out;
        }

        UserTaskRun utr = nr.userTaskRun;
        if (!overrideClaim && utr.specificUserId != null) {
            out.code = LHResponseCodePb.ALREADY_EXISTS_ERROR;
            out.message = "User Task Run already assigned to " + utr.specificUserId;
            return out;
        }

        if (
            utr.status != UserTaskRunStatusPb.CLAIMED &&
            utr.status != UserTaskRunStatusPb.ASSIGNED_NOT_CLAIMED &&
            utr.status != UserTaskRunStatusPb.UNASSIGNED
        ) {
            out.code = LHResponseCodePb.BAD_REQUEST_ERROR;
            out.message =
                "Couldn't reassign User Task Run since it  is in terminal status " +
                utr.status;
        }

        // In the future, we could add some verification to make sure that the
        // user actually exists. For now, this is fine.
        nr.userTaskRun.reassignTo(this);

        wfRun.advance(dao.getEventTime());
        out.code = LHResponseCodePb.OK;
        return out;
    }

    public boolean hasResponse() {
        return true;
    }

    public String getPartitionKey() {
        return wfRunId;
    }
}
