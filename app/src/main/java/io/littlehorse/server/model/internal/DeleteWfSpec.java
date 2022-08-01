package io.littlehorse.server.model.internal;

import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.proto.DeleteWfSpecPb;

public class DeleteWfSpec extends LHSerializable<DeleteWfSpecPb> {
    public String id;

    public Class<DeleteWfSpecPb> getProtoBaseClass() {
        return DeleteWfSpecPb.class;
    }

    public void initFrom(DeleteWfSpecPb proto) {
        id = proto.getId();
    }

    public DeleteWfSpecPb.Builder toProto() {
        return DeleteWfSpecPb.newBuilder().setId(id);
    }

    public static DeleteWfSpec fromProto(DeleteWfSpecPb proto) {
        DeleteWfSpec out = new DeleteWfSpec();
        out.initFrom(proto);
        return out;
    }
}