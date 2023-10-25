package io.littlehorse.common.model.corecommand.subcommand;

import com.google.protobuf.Empty;
import com.google.protobuf.Message;
import io.grpc.Status;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.dao.CoreProcessorDAO;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.corecommand.CoreSubCommand;
import io.littlehorse.common.model.getable.core.usertaskrun.UserTaskRunModel;
import io.littlehorse.common.model.getable.core.variable.VariableValueModel;
import io.littlehorse.common.model.getable.objectId.UserTaskRunIdModel;
import io.littlehorse.sdk.common.proto.CompleteUserTaskRunRequest;
import io.littlehorse.sdk.common.proto.VariableValue;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CompleteUserTaskRunRequestModel extends CoreSubCommand<CompleteUserTaskRunRequest> {

    private UserTaskRunIdModel userTaskRunId;
    private String userId;
    private Map<String, VariableValueModel> results = new HashMap<>();
    private Date time;

    public Class<CompleteUserTaskRunRequest> getProtoBaseClass() {
        return CompleteUserTaskRunRequest.class;
    }

    public CompleteUserTaskRunRequest.Builder toProto() {
        CompleteUserTaskRunRequest.Builder out = CompleteUserTaskRunRequest.newBuilder()
                .setUserTaskRunId(userTaskRunId.toProto())
                .setUserId(userId);
        for (Map.Entry<String, VariableValueModel> entry : results.entrySet()) {
            out.putResults(entry.getKey(), entry.getValue().toProto().build());
        }
        return out;
    }

    public void initFrom(Message proto) {
        CompleteUserTaskRunRequest p = (CompleteUserTaskRunRequest) proto;
        userId = p.getUserId();
        userTaskRunId = LHSerializable.fromProto(p.getUserTaskRunId(), UserTaskRunIdModel.class);

        for (Map.Entry<String, VariableValue> entry : p.getResultsMap().entrySet()) {
            results.put(entry.getKey(), VariableValueModel.fromProto(entry.getValue()));
        }
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
