package io.littlehorse.common.model.getable.global.metrics;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.LHStatusRange;
import io.littlehorse.server.streams.topology.core.ExecutionContext;

public class LHStatusRangeModel extends LHSerializable<LHStatusRange> {

    private LHStatus start;
    private LHStatus end;

    @Override
    public LHStatusRange.Builder toProto() {
        return LHStatusRange.newBuilder().setStarts(start).setEnds(end);
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeException {
        LHStatusRange p = (LHStatusRange) proto;
        start = p.getStarts();
        end = p.getEnds();
    }

    @Override
    public Class<LHStatusRange> getProtoBaseClass() {
        return LHStatusRange.class;
    }
}
