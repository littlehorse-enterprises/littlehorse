package io.littlehorse.common.model.getable.core.wfrun.haltreason;

import org.apache.commons.lang3.NotImplementedException;

import com.google.protobuf.Message;

import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.getable.core.wfrun.ThreadRunModel;
import io.littlehorse.sdk.common.proto.FailingHaltReason;
import io.littlehorse.server.streams.topology.core.ExecutionContext;

public class FailingHaltReasonModel extends LHSerializable<FailingHaltReason> implements SubHaltReason {

    @Override
    public Class<FailingHaltReason> getProtoBaseClass() {
        return FailingHaltReason.class;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext ctx) {}

    @Override
    public FailingHaltReason.Builder toProto() {
        return FailingHaltReason.newBuilder();
    }

    public boolean isResolved(ThreadRunModel haltedThread) {
        throw new NotImplementedException();
    }
}
