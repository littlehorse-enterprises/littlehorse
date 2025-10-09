package io.littlehorse.common.model.getable.objectId;

import java.util.Optional;
import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.getable.CoreObjectId;
import io.littlehorse.common.model.getable.core.taskrun.CheckpointModel;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.sdk.common.proto.Checkpoint;
import io.littlehorse.sdk.common.proto.CheckpointId;
import io.littlehorse.server.streams.topology.core.ExecutionContext;

public class CheckpointIdModel extends CoreObjectId<CheckpointId, Checkpoint, CheckpointModel> {

    private TaskRunIdModel taskRun;
    private int checkpointNumber;

    @Override
    public Class<CheckpointId> getProtoBaseClass() {
        return CheckpointId.class;
    }

    @Override
    public CheckpointId.Builder toProto() {
        return CheckpointId.newBuilder().setTaskRun(taskRun.toProto()).setCheckpointNumber(checkpointNumber);
    }

    @Override
    public void initFrom(Message proto, ExecutionContext ignored) {
        CheckpointId p = (CheckpointId) proto;
        this.taskRun = LHSerializable.fromProto(p.getTaskRun(), TaskRunIdModel.class, ignored);
        this.checkpointNumber = p.getCheckpointNumber();
    }

    @Override
    public Optional<String> getPartitionKey() {
        return taskRun.getPartitionKey();
    }

    @Override
    public GetableClassEnum getType() {
        return GetableClassEnum.CHECKPOINT;
    }

    @Override
    public String toString() {
        return taskRun.toString() + "/" + checkpointNumber;
    }

    @Override
    public void initFromString(String key) {
        int separatorIndex = key.lastIndexOf("/");
        checkpointNumber = Integer.valueOf(key.substring(key.lastIndexOf("/") + 1));
        taskRun = (TaskRunIdModel) TaskRunIdModel.fromString(key.substring(0, separatorIndex), TaskRunIdModel.class);
    }
}
