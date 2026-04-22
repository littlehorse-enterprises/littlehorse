package io.littlehorse.common.model.getable.global.structdef;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.exceptions.validation.TypeValidationException;
import io.littlehorse.common.model.getable.core.variable.StructFieldModel;
import io.littlehorse.common.model.getable.core.variable.VariableValueModel;
import io.littlehorse.common.model.getable.global.wfspec.TypeDefinitionModel;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.StructFieldDef;
import io.littlehorse.server.streams.storeinternals.ReadOnlyMetadataManager;
import io.littlehorse.server.streams.topology.core.ExecutionContext;

public class StructFieldDefModel extends LHSerializable<StructFieldDef> {
    private TypeDefinitionModel fieldType;
    private VariableValueModel defaultValue;
    private boolean isNullable;

    @Override
    public StructFieldDef.Builder toProto() {
        StructFieldDef.Builder out = StructFieldDef.newBuilder()
                .setFieldType(this.fieldType.toProto())
                .setIsNullable(isNullable);
        if (defaultValue != null) {
            out.setDefaultValue(defaultValue.toProto());
        }
        return out;
    }

    @Override
    public void initFrom(Message p, ExecutionContext context) throws LHSerdeException {
        StructFieldDef proto = (StructFieldDef) p;
        fieldType = TypeDefinitionModel.fromProto(proto.getFieldType(), context);
        isNullable = proto.getIsNullable();
        if (proto.hasDefaultValue()) {
            defaultValue = VariableValueModel.fromProto(proto.getDefaultValue(), context);
        }
    }

    public void validateAgainst(StructFieldModel structField, ReadOnlyMetadataManager metadataManager)
            throws StructValidationException {
        structField.setMasked(fieldType.isMasked());
        if (structField.getValue().isNull()) {
            if (!isNullable) {
                throw new StructValidationException("Field is not nullable but received null value");
            }
            return;
        }
        try {
            fieldType.validateCompatibility(structField.getValue(), metadataManager);
        } catch (TypeValidationException e) {
            throw new StructValidationException(e, "StructFieldDef validation failed: " + e.getMessage());
        }
    }

    @Override
    public Class<StructFieldDef> getProtoBaseClass() {
        return StructFieldDef.class;
    }

    public boolean hasDefaultValue() {
        // Nullable fields implicitly default to null if the client does not provide a default.
        return defaultValue != null || isNullable;
    }

    public boolean isRequired() {
        return !hasDefaultValue();
    }

    public void validate(ReadOnlyMetadataManager metadataManager) throws StructDefValidationException {
        // A null default value on a non-nullable field is incoherent: the default would
        // immediately violate the non-null constraint every time it is applied.
        if (defaultValue != null && defaultValue.isNull() && !isNullable) {
            throw new StructDefValidationException("Non-nullable field cannot have a null default value");
        }
        // Validates field type against default value
        if (defaultValue != null && !defaultValue.isNull()) {
            try {
                this.fieldType.validateCompatibility(defaultValue, metadataManager);
            } catch (TypeValidationException e) {
                throw new StructDefValidationException(e, "StructFieldDef validation failed: " + e.getMessage());
            }
        }
    }

    public TypeDefinitionModel getFieldType() {
        return this.fieldType;
    }
}
