package io.littlehorse.common.model.getable.global.metrics;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.TaskRunStatusRange;
import io.littlehorse.sdk.common.proto.TaskStatus;
import io.littlehorse.server.streams.topology.core.ExecutionContext;

public class TaskRunStatusRangeModel extends LHSerializable<TaskRunStatusRange> {
    private TaskStatus start;
    private TaskStatus end;

    @Override
    public TaskRunStatusRange.Builder toProto() {
        return TaskRunStatusRange.newBuilder().setStarts(start).setEnds(end);
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeException {
        TaskRunStatusRange p = (TaskRunStatusRange) proto;
        start = p.getStarts();
        end = p.getEnds();
    }

    @Override
    public Class<TaskRunStatusRange> getProtoBaseClass() {
        return TaskRunStatusRange.class;
    }
}
