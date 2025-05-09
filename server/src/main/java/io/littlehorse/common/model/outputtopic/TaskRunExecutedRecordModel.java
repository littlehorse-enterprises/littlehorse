package io.littlehorse.common.model.outputtopic;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.getable.core.taskrun.TaskRunModel;
import io.littlehorse.sdk.common.proto.TaskRunExecutedRecord;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
public class TaskRunExecutedRecordModel extends LHSerializable<TaskRunExecutedRecord>
        implements GenericOutputTopicRecordModel {

    private TaskRunModel getable;

    @Override
    public Class<TaskRunExecutedRecord> getProtoBaseClass() {
        return TaskRunExecutedRecord.class;
    }

    @Override
    public TaskRunExecutedRecord.Builder toProto() {
        TaskRunExecutedRecord.Builder result = TaskRunExecutedRecord.newBuilder();

        result.setTaskRun(getable.toProto());

        return result;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext ignored) {
        TaskRunExecutedRecord p = (TaskRunExecutedRecord) proto;

        this.getable = LHSerializable.fromProto(p.getTaskRun(), TaskRunModel.class, ignored);
    }

    @Override
    public String getPartitionKey() {
        return getable.getPartitionKey().get();
    }
}
