package io.littlehorse.common.model.command.subcommand;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.LHDAO;
import io.littlehorse.common.model.command.SubCommand;
import io.littlehorse.common.model.command.subcommandresponse.AssignUserTaskRunReply;
import io.littlehorse.jlib.common.proto.AssignUserTaskRunPb;
import io.littlehorse.jlib.common.proto.AssignUserTaskRunPb.AssigneeCase;
import io.littlehorse.jlib.common.proto.UserGroupsPb;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.NotImplementedException;

@Slf4j
public class AssignUserTaskRun extends SubCommand<AssignUserTaskRunPb> {

    public String wfRunId;
    public int threadRunNumber;
    public int nodeRunPosition;

    public AssigneeCase assigneeType;
    public String userId;
    public UserGroupsPb groups;

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
            case GROUPS:
                out.setGroups(groups);
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
            case GROUPS:
                groups = p.getGroups();
                break;
            case ASSIGNEE_NOT_SET:
                log.warn("Unset assignee. Should this be error?");
                break;
        }
    }

    public AssignUserTaskRunReply process(LHDAO dao, LHConfig config) {
        throw new NotImplementedException();
    }

    public boolean hasResponse() {
        return true;
    }

    public String getPartitionKey() {
        return wfRunId;
    }
}
