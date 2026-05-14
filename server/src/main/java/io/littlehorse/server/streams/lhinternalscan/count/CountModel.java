package io.littlehorse.server.streams.lhinternalscan.count;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.Count;
import io.littlehorse.server.streams.topology.core.ExecutionContext;

public class CountModel extends LHSerializable<Count> {

    private long count;

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeException {
        Count p = (Count) proto;
        count = p.getValue();
    }

    @Override
    public Count.Builder toProto() {
        return Count.newBuilder().setValue(count);
    }

    @Override
    public Class<Count> getProtoBaseClass() {
        return Count.class;
    }

    public void setCount(long count) {
        this.count = count;
    }
}
