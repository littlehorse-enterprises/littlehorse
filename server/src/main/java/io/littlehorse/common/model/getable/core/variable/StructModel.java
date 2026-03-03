package io.littlehorse.common.model.getable.core.variable;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.getable.global.structdef.InlineStructDefModel;
import io.littlehorse.common.model.getable.global.structdef.StructDefModel;
import io.littlehorse.common.model.getable.global.structdef.StructFieldDefModel;
import io.littlehorse.common.model.getable.global.structdef.StructValidationException;
import io.littlehorse.common.model.getable.objectId.StructDefIdModel;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.InlineStruct;
import io.littlehorse.sdk.common.proto.Struct;
import io.littlehorse.sdk.common.proto.StructField;
import io.littlehorse.server.streams.storeinternals.ReadOnlyMetadataManager;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.topology.core.RequestExecutionContext;
import io.littlehorse.server.streams.topology.core.WfService;
import java.util.Arrays;
import java.util.Map;
import lombok.Getter;

@Getter
public class StructModel extends LHSerializable<Struct> implements Comparable<StructModel> {

    private StructDefIdModel structDefId;

    private InlineStructModel inlineStruct;

    private ExecutionContext context;

    private boolean maskOnSerialize;

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeException {
        Struct p = (Struct) proto;

        this.structDefId = StructDefIdModel.fromProto(p.getStructDefId(), context);
        this.inlineStruct = InlineStructModel.fromProto(p.getStruct(), InlineStructModel.class, context);
        this.context = context;
        this.maskOnSerialize = context != null && context.support(RequestExecutionContext.class);
    }

    private InlineStruct.Builder maskedInlineStructProto() {
        InlineStruct.Builder maskedStruct = InlineStruct.newBuilder();

        for (Map.Entry<String, StructFieldModel> entry :
                inlineStruct.getFields().entrySet()) {
            StructField.Builder fieldBuilder = entry.getValue().toProto();

            if (shouldMaskField(entry.getKey())) {
                fieldBuilder.setValue(new VariableValueModel(LHConstants.STRING_MASK).toProto());
            }

            maskedStruct.putFields(entry.getKey(), fieldBuilder.build());
        }

        return maskedStruct;
    }

    private boolean shouldMaskField(String fieldName) {
        if (!maskOnSerialize || context == null || !context.support(RequestExecutionContext.class)) {
            return false;
        }

        StructDefModel structDef = context.service().getStructDef(structDefId);
        if (structDef == null) {
            return false;
        }

        InlineStructDefModel structDefFields = structDef.getStructDef();
        StructFieldDefModel fieldDef = structDefFields.getFields().get(fieldName);

        return fieldDef != null && fieldDef.getFieldType().isMasked();
    }

    @Override
    public Struct.Builder toProto() {
        Struct.Builder out = Struct.newBuilder();

        out.setStructDefId(structDefId.toProto());
        out.setStruct(maskedInlineStructProto());

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

    public void disableResponseMaskingRecursively() {
        this.maskOnSerialize = false;

        if (inlineStruct == null || inlineStruct.getFields() == null) {
            return;
        }

        for (StructFieldModel field : inlineStruct.getFields().values()) {
            if (field.getValue() != null) {
                field.getValue().disableResponseMaskingRecursively();
            }
        }
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
