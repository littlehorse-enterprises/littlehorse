package io.littlehorse.common.model.corecommand.subcommand;

import com.google.protobuf.Empty;
import com.google.protobuf.Message;
import io.grpc.Status;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.dao.CoreProcessorDAO;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.corecommand.SubCommand;
import io.littlehorse.common.model.getable.core.usertaskrun.UserTaskRunModel;
import io.littlehorse.common.model.getable.objectId.UserTaskRunIdModel;
import io.littlehorse.sdk.common.proto.CompleteUserTaskRunRequest;
import io.littlehorse.sdk.common.proto.UserTaskResult;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CompleteUserTaskRunRequestModel extends SubCommand<CompleteUserTaskRunRequest> {

    private UserTaskRunIdModel userTaskRunId;
    private String userId;
    private UserTaskResult result;
    private Date time;

    public Class<CompleteUserTaskRunRequest> getProtoBaseClass() {
        return CompleteUserTaskRunRequest.class;
    }

    public CompleteUserTaskRunRequest.Builder toProto() {
        CompleteUserTaskRunRequest.Builder out = CompleteUserTaskRunRequest.newBuilder()
                .setUserTaskRunId(userTaskRunId.toProto())
                .setUserId(userId)
                .setResult(result);
        return out;
    }

    public void initFrom(Message proto) {
        CompleteUserTaskRunRequest p = (CompleteUserTaskRunRequest) proto;
        userId = p.getUserId();
        userTaskRunId = LHSerializable.fromProto(p.getUserTaskRunId(), UserTaskRunIdModel.class);
        result = p.getResult();
    }

    public Empty process(CoreProcessorDAO dao, LHServerConfig config) {
        UserTaskRunModel utr = dao.get(userTaskRunId);
        if (utr == null) {
            throw new LHApiException(Status.NOT_FOUND, "Couldn't find provided UserTaskRun");
        }

        utr.processTaskCompletedEvent(this);
        return Empty.getDefaultInstance();
    }

    public boolean hasResponse() {
        return true;
    }

    public String getWfRunId() {
        return userTaskRunId.getWfRunId();
    }

    public String getPartitionKey() {
        return getWfRunId();
    }
}
