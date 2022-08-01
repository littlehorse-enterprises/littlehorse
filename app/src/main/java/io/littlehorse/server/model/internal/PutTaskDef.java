package io.littlehorse.server.model.internal;

import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.model.meta.TaskDef;
import io.littlehorse.common.proto.PutTaskDefPb;

public class PutTaskDef extends LHSerializable<PutTaskDefPb> {
    public TaskDef spec;

    public Class<PutTaskDefPb> getProtoBaseClass() {
        return PutTaskDefPb.class;
    }

    public void initFrom(PutTaskDefPb proto) {
        spec = new TaskDef();
        spec.initFrom(proto.getSpec());
    }

    public PutTaskDefPb.Builder toProto() {
        return PutTaskDefPb.newBuilder().setSpec(spec.toProto());
    }

    public static PutTaskDef fromProto(PutTaskDefPb proto) {
        PutTaskDef out = new PutTaskDef();
        out.initFrom(proto);
        return out;
    }
}
