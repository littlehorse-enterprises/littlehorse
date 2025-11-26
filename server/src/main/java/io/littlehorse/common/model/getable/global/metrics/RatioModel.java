package io.littlehorse.common.model.getable.global.metrics;

import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.Aggregator;
import io.littlehorse.server.streams.topology.core.ExecutionContext;

public class RatioModel extends LHSerializable<Aggregator.Ratio> {

    private StatusRangeModel statusRange;

    @Override
    public Aggregator.Ratio.Builder toProto() {
        return Aggregator.Ratio.newBuilder().setStatusRange(statusRange.toProto());
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeException {
        Aggregator.Ratio p = (Aggregator.Ratio) proto;
        statusRange = LHSerializable.fromProto(p.getStatusRange(), StatusRangeModel.class, context);
    }

    @Override
    public Class<Aggregator.Ratio> getProtoBaseClass() {
        return Aggregator.Ratio.class;
    }
}
