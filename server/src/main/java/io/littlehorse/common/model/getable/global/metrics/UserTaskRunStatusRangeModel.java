package io.littlehorse.common.model.getable.global.metrics;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.UserTaskRunStatus;
import io.littlehorse.sdk.common.proto.UserTaskRunStatusRange;
import io.littlehorse.server.streams.topology.core.ExecutionContext;

public class UserTaskRunStatusRangeModel extends LHSerializable<UserTaskRunStatusRange> {

    private UserTaskRunStatus start;
    private UserTaskRunStatus end;

    @Override
    public UserTaskRunStatusRange.Builder toProto() {
        return UserTaskRunStatusRange.newBuilder().setStarts(start).setEnds(end);
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeException {
        UserTaskRunStatusRange p = (UserTaskRunStatusRange) proto;
        start = p.getStarts();
        end = p.getEnds();
    }

    @Override
    public Class<UserTaskRunStatusRange> getProtoBaseClass() {
        return UserTaskRunStatusRange.class;
    }
}
