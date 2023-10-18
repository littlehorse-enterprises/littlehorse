package io.littlehorse.common.model.corecommand.subcommand;

import com.google.protobuf.Empty;
import com.google.protobuf.Message;
import io.grpc.Status;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.dao.CoreProcessorDAO;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.corecommand.SubCommand;
import io.littlehorse.common.model.getable.core.usertaskrun.UserTaskRunModel;
import io.littlehorse.common.model.getable.core.wfrun.WfRunModel;
import io.littlehorse.common.model.getable.objectId.UserTaskRunIdModel;
import io.littlehorse.sdk.common.proto.AssignUserTaskRunRequest;
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

    private String userId;
    private String userGroup;

    public Class<AssignUserTaskRunRequest> getProtoBaseClass() {
        return AssignUserTaskRunRequest.class;
    }

    public AssignUserTaskRunRequest.Builder toProto() {
        AssignUserTaskRunRequest.Builder out = AssignUserTaskRunRequest.newBuilder()
                .setUserTaskRunId(userTaskRunId.toProto())
                .setOverrideClaim(overrideClaim);
        if (userGroup != null) out.setUserGroup(userGroup);
        if (userId != null) out.setUserId(userId);
        return out;
    }

    public void initFrom(Message proto) {
        AssignUserTaskRunRequest p = (AssignUserTaskRunRequest) proto;
        userTaskRunId = LHSerializable.fromProto(p.getUserTaskRunId(), UserTaskRunIdModel.class);
        overrideClaim = p.getOverrideClaim();

        if (p.hasUserGroup()) userGroup = p.getUserGroup();
        if (p.hasUserId()) userId = p.getUserId();
    }

    public String getWfRunId() {
        return userTaskRunId.getWfRunId();
    }

    @Override
    public Empty process(CoreProcessorDAO dao, LHServerConfig config, String tenantId) {

        if (userGroup == null && userId == null) {
            throw new LHApiException(Status.INVALID_ARGUMENT, "must provide either UserGroup or userId");
        }

        UserTaskRunModel utr = dao.get(userTaskRunId);
        if (utr == null) {
            throw new LHApiException(Status.NOT_FOUND, "Couldn't find UserTaskRun " + userTaskRunId);
        }

        if (!overrideClaim && utr.getUserId() != null) {
            throw new LHApiException(
                    Status.FAILED_PRECONDITION, "User Task Run already assigned to " + utr.getUserId());
        }

        if (utr.isTerminated()) {
            throw new LHApiException(
                    Status.FAILED_PRECONDITION,
                    "Couldn't reassign User Task Run since it  is in terminal status " + utr.getStatus());
        }

        // LittleHorse currently does not store users, as such we cannot verify whether the user/userGroup
        // are valid values.

        log.debug("Reassigning user task run {} to user: {}, group: {}", userTaskRunId, userId, userGroup);
        utr.assignTo(userId, userGroup, true);

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
