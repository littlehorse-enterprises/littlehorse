package io.littlehorse.server.streams.lhinternalscan.count;

import com.google.protobuf.Message;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.CountTaskRunRequest;
import io.littlehorse.sdk.common.proto.TaskStatus;
import io.littlehorse.server.streams.storeinternals.index.Attribute;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.List;

public class CountTaskRunRequestModel extends CountRequest<CountTaskRunRequest> {

    private String taskDefName;
    private TaskStatus status;

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeException {
        CountTaskRunRequest p = (CountTaskRunRequest) proto;
        taskDefName = p.getTaskDefName();
        status = p.getStatus();
    }

    @Override
    public CountTaskRunRequest.Builder toProto() {
        return CountTaskRunRequest.newBuilder()
                .setTaskDefName(taskDefName)
                .setStatus(status);
    }

    @Override
    public Class<CountTaskRunRequest> getProtoBaseClass() {
        return CountTaskRunRequest.class;
    }

    @Override
    protected GetableClassEnum getObjectType() {
        return GetableClassEnum.TASK_RUN;
    }

    @Override
    protected List<Attribute> countAttributes() {
        if (taskDefName == null || taskDefName.isEmpty()) {
            throw new IllegalArgumentException("taskDefName is required");
        }
        if (status != TaskStatus.TASK_SCHEDULED) {
            throw new IllegalArgumentException(
                    "Only TASK_SCHEDULED status is currently supported for counting TaskRuns");
        }
        return List.of(
                new Attribute("scheduled", TaskStatus.TASK_SCHEDULED.toString()),
                new Attribute("taskDefName", taskDefName));
    }
}
