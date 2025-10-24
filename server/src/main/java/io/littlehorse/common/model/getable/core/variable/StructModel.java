package io.littlehorse.common.model.getable.core.variable;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.getable.global.structdef.StructDefModel;
import io.littlehorse.common.model.getable.global.structdef.StructValidationException;
import io.littlehorse.common.model.getable.objectId.StructDefIdModel;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.Struct;
import io.littlehorse.server.streams.storeinternals.ReadOnlyMetadataManager;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.topology.core.WfService;
import lombok.Getter;

@Getter
public class StructModel extends LHSerializable<Struct> {

    private StructDefIdModel structDefId;

    private InlineStructModel inlineStruct;

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeException {
        Struct p = (Struct) proto;

        this.structDefId = StructDefIdModel.fromProto(p.getStructDefId(), context);
        this.inlineStruct = InlineStructModel.fromProto(p.getStruct(), InlineStructModel.class, context);
    }

    @Override
    public Struct.Builder toProto() {
        Struct.Builder out = Struct.newBuilder();

        out.setStructDefId(structDefId.toProto());
        out.setStruct(inlineStruct.toProto());

        return out;
    }

    public void validateAgainstStructDefId(ReadOnlyMetadataManager metadataManager) throws StructValidationException {
        StructDefModel structDef = new WfService(metadataManager).getStructDef(structDefId.getName(), null);

        if (structDef == null) {
            throw new StructValidationException("StructDef %s does not exist.".formatted(structDefId));
        }

        try {
            structDef.validateAgainst(this, metadataManager);
        } catch (StructValidationException e) {
            throw new StructValidationException(String.format(
                    "Struct incompatible with StructDef %s: %s", structDef.getObjectId(), e.getMessage()));
        }
    }

    @Override
    public Class<Struct> getProtoBaseClass() {
        return Struct.class;
    }
}
