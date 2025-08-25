package io.littlehorse.common.model.getable.global.metrics;

import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.Aggregator;
import io.littlehorse.server.streams.topology.core.ExecutionContext;

public class StatusRangeModel extends LHSerializable<Aggregator.StatusRange> {

    private LHStatusRangeModel lhStatusRange;
    private TaskRunStatusRangeModel taskRunStatusRange;
    private UserTaskRunStatusRangeModel userTaskRunStatusRange;
    private Aggregator.StatusRange.TypeCase typeCase;

    @Override
    public Aggregator.StatusRange.Builder toProto() {
        Aggregator.StatusRange.Builder out = Aggregator.StatusRange.newBuilder();
        if (lhStatusRange != null) {
            out.setLhStatus(lhStatusRange.toProto());
        }
        if (taskRunStatusRange != null) {
            out.setTaskRun(taskRunStatusRange.toProto());
        }
        if (userTaskRunStatusRange != null) {
            out.setUserTaskRun(userTaskRunStatusRange.toProto());
        }
        return out;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeException {
        Aggregator.StatusRange out = (Aggregator.StatusRange) proto;
        typeCase = out.getTypeCase();
        if (out.hasLhStatus()) {
            lhStatusRange = LHSerializable.fromProto(out.getLhStatus(), LHStatusRangeModel.class, context);
        }
        if (out.hasTaskRun()) {
            taskRunStatusRange = LHSerializable.fromProto(out.getTaskRun(), TaskRunStatusRangeModel.class, context);
        }
        if (out.hasUserTaskRun()) {
            userTaskRunStatusRange =
                    LHSerializable.fromProto(out.getUserTaskRun(), UserTaskRunStatusRangeModel.class, context);
        }
    }

    @Override
    public Class<? extends GeneratedMessageV3> getProtoBaseClass() {
        return Aggregator.StatusRange.class;
    }
}
