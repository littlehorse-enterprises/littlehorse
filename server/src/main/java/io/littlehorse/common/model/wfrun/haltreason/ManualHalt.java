package io.littlehorse.common.model.wfrun.haltreason;

import com.google.protobuf.Message;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.model.wfrun.WfRunModel;
import io.littlehorse.sdk.common.proto.ManualHaltPb;

public class ManualHalt
    extends LHSerializable<ManualHaltPb>
    implements SubHaltReason {

    public boolean meaningOfLife;

    public boolean isResolved(WfRunModel wfRunModel) {
        // never resolved; only removed.
        return false;
    }

    public Class<ManualHaltPb> getProtoBaseClass() {
        return ManualHaltPb.class;
    }

    public ManualHaltPb.Builder toProto() {
        ManualHaltPb.Builder out = ManualHaltPb.newBuilder();
        out.setMeaningOfLife(meaningOfLife);
        return out;
    }

    public void initFrom(Message proto) {
        ManualHaltPb p = (ManualHaltPb) proto;
        meaningOfLife = p.getMeaningOfLife();
    }

    public static ManualHalt fromProto(ManualHaltPb proto) {
        ManualHalt out = new ManualHalt();
        out.initFrom(proto);
        return out;
    }
}
