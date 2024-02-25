package io.littlehorse.common.model.getable.core.wfrun.haltreason;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.getable.core.wfrun.ThreadRunModel;
import io.littlehorse.sdk.common.proto.ManualHalt;
import io.littlehorse.server.streams.topology.core.ExecutionContext;

public class ManualHaltModel extends LHSerializable<ManualHalt> implements SubHaltReason {

    public boolean meaningOfLife;

    public boolean isResolved(ThreadRunModel haltedThread) {
        // never resolved; only removed.
        return false;
    }

    public Class<ManualHalt> getProtoBaseClass() {
        return ManualHalt.class;
    }

    public ManualHalt.Builder toProto() {
        ManualHalt.Builder out = ManualHalt.newBuilder();
        out.setMeaningOfLife(meaningOfLife);
        return out;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        ManualHalt p = (ManualHalt) proto;
        meaningOfLife = p.getMeaningOfLife();
    }

    public static ManualHaltModel fromProto(ManualHalt proto, ExecutionContext context) {
        ManualHaltModel out = new ManualHaltModel();
        out.initFrom(proto, context);
        return out;
    }
}
