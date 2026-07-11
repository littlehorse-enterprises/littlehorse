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
import java.util.Arrays;
import lombok.Getter;
import lombok.Setter;

@Getter
public class StructModel extends LHSerializable<Struct> implements Comparable<StructModel> {

    @Setter
    private StructDefIdModel structDefId;

    @Setter
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

    public void validateAgainstStructDefId(
            StructDefIdModel expectedStructDefId, ReadOnlyMetadataManager metadataManager)
            throws StructValidationException {
        if (expectedStructDefId == null) {
            throw new StructValidationException("Expected StructDefId cannot be null");
        }

        StructDefModel structDef = new WfService(metadataManager).getStructDef(expectedStructDefId);

        if (structDef == null) {
            throw new StructValidationException("StructDef %s does not exist.".formatted(expectedStructDefId));
        }

        validateAgainstStructDef(structDef, expectedStructDefId, metadataManager);
    }

    /**
     * Validates this struct against an already-resolved StructDef. Callers that validate many structs
     * against the same StructDef (e.g. an array or map of structs) should resolve the StructDef once
     * and reuse it via this method rather than re-resolving it per element.
     */
    public void validateAgainstStructDef(
            StructDefModel structDef, StructDefIdModel expectedStructDefId, ReadOnlyMetadataManager metadataManager)
            throws StructValidationException {
        try {
            structDef.validateAgainstSuperset(this, metadataManager);
        } catch (StructValidationException e) {
            throw new StructValidationException(String.format(
                    "Struct incompatible with StructDef %s: %s", structDef.getObjectId(), e.getMessage()));
        }

        this.structDefId = expectedStructDefId;
    }

    @Override
    public Class<Struct> getProtoBaseClass() {
        return Struct.class;
    }

    // TODO: This is an incomplete implementation of a compareTo() method
    // We should greatly refactor how comparisons are made on the server to restrict
    // the use of comparators on certain types (Structs should not support LESS_THAN/GREATER_THAN)
    @Override
    public int compareTo(StructModel o) {
        if (o == null) return -1;

        return Arrays.compare(
                this.toProto().build().toByteArray(), o.toProto().build().toByteArray());
    }
}
