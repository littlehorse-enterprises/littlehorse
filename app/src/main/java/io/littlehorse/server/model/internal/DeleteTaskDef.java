package io.littlehorse.server.model.internal;

import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.proto.DeleteTaskDefPb;

public class DeleteTaskDef extends LHSerializable<DeleteTaskDefPb> {
    public String id;

    public Class<DeleteTaskDefPb> getProtoBaseClass() {
        return DeleteTaskDefPb.class;
    }

    public void initFrom(DeleteTaskDefPb proto) {
        id = proto.getId();
    }

    public DeleteTaskDefPb.Builder toProto() {
        return DeleteTaskDefPb.newBuilder().setId(id);
    }

    public static DeleteTaskDef fromProto(DeleteTaskDefPb proto) {
        DeleteTaskDef out = new DeleteTaskDef();
        out.initFrom(proto);
        return out;
    }
}
