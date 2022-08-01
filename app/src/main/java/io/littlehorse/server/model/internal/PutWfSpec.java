package io.littlehorse.server.model.internal;

import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.model.meta.WfSpec;
import io.littlehorse.common.proto.PutWfSpecPb;

public class PutWfSpec extends LHSerializable<PutWfSpecPb> {
    public WfSpec spec;

    public Class<PutWfSpecPb> getProtoBaseClass() {
        return PutWfSpecPb.class;
    }

    public void initFrom(PutWfSpecPb proto) {
        spec = new WfSpec();
        spec.initFrom(proto.getSpec());
    }

    public PutWfSpecPb.Builder toProto() {
        return PutWfSpecPb.newBuilder().setSpec(spec.toProto());
    }

    public static PutWfSpec fromProto(PutWfSpecPb proto) {
        PutWfSpec out = new PutWfSpec();
        out.initFrom(proto);
        return out;
    }
}
