package io.littlehorse.common.model.getable.global.wfspec.variable;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.getable.objectId.StructDefIdModel;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.Struct;
import io.littlehorse.server.streams.topology.core.ExecutionContext;

public class StructModel extends LHSerializable<Struct> {

    private StructDefIdModel structDefId;
    private InlineStructModel struct;

    @Override
    public Struct.Builder toProto() {
        Struct.Builder out = Struct.newBuilder();

        out.setStructDefId(structDefId.toProto());
        out.setStruct(struct.toProto());

        return out;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext ctx) throws LHSerdeException {
        Struct p = (Struct) proto;

        structDefId = StructDefIdModel.fromProto(p.getStructDefId(), ctx);
        struct = InlineStructModel.fromProto(p.getStruct(), ctx);
    }

    public static StructModel fromProto(Struct proto, ExecutionContext context) {
        StructModel out = new StructModel();
        out.initFrom(proto, context);
        return out;
    }

    @Override
    public Class<Struct> getProtoBaseClass() {
        return Struct.class;
    }
}
