package io.littlehorse.server.streams.lhinternalscan.count;

import com.google.protobuf.Message;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.CountScheduledTaskRunRequest;
import io.littlehorse.sdk.common.proto.TaskStatus;
import io.littlehorse.server.streams.storeinternals.index.Attribute;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.List;

public class CountScheduledTaskRunRequestModel extends CountRequest<CountScheduledTaskRunRequest> {

    private String taskDefName;

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeException {
        CountScheduledTaskRunRequest p = (CountScheduledTaskRunRequest) proto;
        taskDefName = p.getTaskDefName();
    }

    @Override
    public CountScheduledTaskRunRequest.Builder toProto() {
        return CountScheduledTaskRunRequest.newBuilder().setTaskDefName(taskDefName);
    }

    @Override
    public Class<CountScheduledTaskRunRequest> getProtoBaseClass() {
        return CountScheduledTaskRunRequest.class;
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
        return List.of(
                new Attribute("scheduled", TaskStatus.TASK_SCHEDULED.toString()),
                new Attribute("taskDefName", taskDefName));
    }
}
