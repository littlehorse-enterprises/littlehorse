package io.littlehorse.common.model.command.subcommand;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.LHDAO;
import io.littlehorse.common.model.command.SubCommand;
import io.littlehorse.common.model.command.subcommandresponse.AssignUserTaskRunReply;
import io.littlehorse.common.model.meta.WfSpec;
import io.littlehorse.common.model.wfrun.WfRun;
import io.littlehorse.jlib.common.proto.AssignUserTaskRunPb;
import io.littlehorse.jlib.common.proto.AssignUserTaskRunPb.AssigneeCase;
import io.littlehorse.jlib.common.proto.LHResponseCodePb;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AssignUserTaskRun extends SubCommand<AssignUserTaskRunPb> {

    public String wfRunId;
    public int threadRunNumber;
    public int nodeRunPosition;

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
            .setNodeRunPosition(nodeRunPosition);

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

        if (wfRun == null) {
            out.code = LHResponseCodePb.BAD_REQUEST_ERROR;
            out.message = "Provided invalid wfRunId";
            return out;
        }

        WfSpec wfSpec = dao.getWfSpec(wfRun.wfSpecName, wfRun.wfSpecVersion);
        if (wfSpec == null) {
            wfRun.failDueToWfSpecDeletion();
            out.code = LHResponseCodePb.NOT_FOUND_ERROR;
            out.message = "Apparently WfSpec was deleted!";
            return out;
        }

        wfRun.wfSpec = wfSpec;
        wfRun.processAssignUserTaskRun(this);

        // TODO: We don't really check to see if the incoming was valid.
        // For example, we should probably check to make sure that the specified
        // node was actually a user task node...especially if customers are writing
        // their own clients (whereas with Task Workers it is our own code).

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
