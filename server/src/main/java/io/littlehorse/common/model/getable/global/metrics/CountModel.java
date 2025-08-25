package io.littlehorse.common.model.getable.global.metrics;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.Aggregator;
import io.littlehorse.server.streams.topology.core.ExecutionContext;

public class CountModel extends LHSerializable<Aggregator.Count> {

    private StatusRangeModel statusRange;

    @Override
    public Aggregator.Count.Builder toProto() {
        return Aggregator.Count.newBuilder().setStatusRange(statusRange.toProto());
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeException {
        Aggregator.Count p = (Aggregator.Count) proto;
        statusRange = LHSerializable.fromProto(p.getStatusRange(), StatusRangeModel.class, context);
    }

    @Override
    public Class<Aggregator.Count> getProtoBaseClass() {
        return Aggregator.Count.class;
    }
}
