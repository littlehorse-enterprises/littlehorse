package io.littlehorse.common.model.corecommand.subcommand;

import com.google.protobuf.Message;
import io.grpc.Status;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.corecommand.CoreSubCommand;
import io.littlehorse.common.model.getable.core.usertaskrun.UserTaskRunModel;
import io.littlehorse.common.model.getable.core.variable.VariableValueModel;
import io.littlehorse.common.model.getable.objectId.UserTaskRunIdModel;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.SaveUserTaskRunProgressRequest;
import io.littlehorse.sdk.common.proto.SaveUserTaskRunProgressRequest.SaveUserTaskRunAssignmentPolicy;
import io.littlehorse.sdk.common.proto.UserTaskRun;
import io.littlehorse.sdk.common.proto.VariableValue;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.topology.core.CoreProcessorContext;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;

@Getter
public class SaveUserTaskRunProgressRequestModel extends CoreSubCommand<SaveUserTaskRunProgressRequest> {

    private UserTaskRunIdModel userTaskRunId;
    private Map<String, VariableValueModel> results;
    private String userId;
    private SaveUserTaskRunAssignmentPolicy policy;

    public SaveUserTaskRunProgressRequestModel() {
        this.results = new HashMap<>();
    }

    @Override
    public Class<SaveUserTaskRunProgressRequest> getProtoBaseClass() {
        return SaveUserTaskRunProgressRequest.class;
    }

    @Override
    public SaveUserTaskRunProgressRequest.Builder toProto() {
        SaveUserTaskRunProgressRequest.Builder builder = SaveUserTaskRunProgressRequest.newBuilder()
                .setUserId(userId)
                .setPolicy(policy)
                .setUserTaskRunId(userTaskRunId.toProto());
        for (Map.Entry<String, VariableValueModel> entry : results.entrySet()) {
            builder.putResults(entry.getKey(), entry.getValue().toProto().build());
        }
        return builder;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeException {
        SaveUserTaskRunProgressRequest p = (SaveUserTaskRunProgressRequest) proto;
        userTaskRunId = UserTaskRunIdModel.fromProto(p.getUserTaskRunId(), UserTaskRunIdModel.class, context);
        userId = p.getUserId();
        policy = p.getPolicy();
        for (Map.Entry<String, VariableValue> entry : p.getResultsMap().entrySet()) {
            VariableValueModel model = VariableValueModel.fromProto(entry.getValue(), context);
            results.put(entry.getKey(), model);
        }
    }

    @Override
    public UserTaskRun process(CoreProcessorContext executionContext, LHServerConfig config) {
        UserTaskRunModel utr = executionContext.getableManager().get(userTaskRunId);
        if (utr == null) {
            throw new LHApiException(Status.NOT_FOUND, "Couldn't find provided UserTaskRun");
        }

        // Validate that the user is permitted to save the progress
        if (policy == SaveUserTaskRunAssignmentPolicy.FAIL_IF_CLAIMED_BY_OTHER) {
            if (utr.getUserId() != null && !userId.equals(utr.getUserId())) {
                throw new LHApiException(Status.FAILED_PRECONDITION, "UserTaskRun is assigned to another user");
            }
        }

        utr.processProgressSavedEvent(this, executionContext);

        // No need to call WfRunModel#advance() since saving the progress of a UserTaskRun
        // will never cause any ThreadRun's to advance.

        return utr.toProto().build();
    }

    @Override
    public String getPartitionKey() {
        return userTaskRunId.getPartitionKey().get();
    }
}
