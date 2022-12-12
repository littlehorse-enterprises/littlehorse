package io.littlehorse.common.model.wfrun.haltreason;

import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.model.wfrun.WfRun;
import io.littlehorse.common.proto.ManualHaltPb;
import io.littlehorse.common.proto.ManualHaltPbOrBuilder;

public class ManualHalt
    extends LHSerializable<ManualHaltPb>
    implements SubHaltReason {

    public boolean meaningOfLife;

    public boolean isResolved(WfRun wfRun) {
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

    public void initFrom(MessageOrBuilder proto) {
        ManualHaltPbOrBuilder p = (ManualHaltPbOrBuilder) proto;
        meaningOfLife = p.getMeaningOfLife();
    }

    public static ManualHalt fromProto(ManualHaltPbOrBuilder proto) {
        ManualHalt out = new ManualHalt();
        out.initFrom(proto);
        return out;
    }
}
