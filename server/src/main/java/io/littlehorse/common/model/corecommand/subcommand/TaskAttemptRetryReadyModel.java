package io.littlehorse.common.model.corecommand.subcommand;

import com.google.protobuf.Empty;
import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.model.corecommand.CoreSubCommand;
import io.littlehorse.common.model.getable.core.taskrun.TaskRunModel;
import io.littlehorse.common.model.getable.objectId.TaskRunIdModel;
import io.littlehorse.common.proto.TaskAttemptRetryReady;
import io.littlehorse.server.streams.topology.core.CoreProcessorContext;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.Date;
import lombok.Getter;

@Getter
public class TaskAttemptRetryReadyModel extends CoreSubCommand<TaskAttemptRetryReady> {

    private TaskRunIdModel id;

    public TaskAttemptRetryReadyModel() {}

    public TaskAttemptRetryReadyModel(TaskRunIdModel id) {
        this.id = id;
    }

    @Override
    public Class<TaskAttemptRetryReady> getProtoBaseClass() {
        return TaskAttemptRetryReady.class;
    }

    @Override
    public TaskAttemptRetryReady.Builder toProto() {
        TaskAttemptRetryReady.Builder out = TaskAttemptRetryReady.newBuilder().setId(id.toProto());

        return out;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext ctx) {
        TaskAttemptRetryReady p = (TaskAttemptRetryReady) proto;
        id = LHSerializable.fromProto(p.getId(), TaskRunIdModel.class, ctx);
    }

    @Override
    public Empty process(CoreProcessorContext context, LHServerConfig config) {
        TaskRunModel taskRun = context.getableManager().get(id);
        Date time = new Date();
        taskRun.markAttemptReadyToSchedule();
        taskRun.getWfRun().advance(time);
        return Empty.getDefaultInstance();
    }

    @Override
    public String getPartitionKey() {
        return id.getPartitionKey().get();
    }
}
