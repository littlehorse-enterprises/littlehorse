package io.littlehorse.common.model.getable.core.wfrun.haltreason;

import com.google.protobuf.Message;

import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.getable.core.wfrun.WfRunModel;
import io.littlehorse.sdk.common.proto.ManualHalt;

public class ManualHaltModel extends LHSerializable<ManualHalt> implements SubHaltReason {

    public boolean meaningOfLife;

    public boolean isResolved(WfRunModel wfRunModel) {
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

    public void initFrom(Message proto) {
        ManualHalt p = (ManualHalt) proto;
        meaningOfLife = p.getMeaningOfLife();
    }

    public static ManualHaltModel fromProto(ManualHalt proto) {
        ManualHaltModel out = new ManualHaltModel();
        out.initFrom(proto);
        return out;
    }
}
