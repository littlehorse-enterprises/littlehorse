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
import lombok.EqualsAndHashCode;
import lombok.Getter;

@EqualsAndHashCode(callSuper = false)
public class StructFieldDefModel extends LHSerializable<StructFieldDef> {

    @Getter
    private TypeDefinitionModel fieldType;

    private VariableValueModel defaultValue;

    @Override
    public StructFieldDef.Builder toProto() {
        StructFieldDef.Builder out = StructFieldDef.newBuilder().setFieldType(this.fieldType.toProto());

        if (defaultValue != null) {
            out.setDefaultValue(defaultValue.toProto());
        }

        return out;
    }

    @Override
    public void initFrom(Message p, ExecutionContext context) throws LHSerdeException {
        StructFieldDef proto = (StructFieldDef) p;
        fieldType = TypeDefinitionModel.fromProto(proto.getFieldType(), context);

        if (proto.hasDefaultValue()) {
            defaultValue = VariableValueModel.fromProto(proto.getDefaultValue(), context);
        }
    }

    public void validateAgainst(StructFieldModel structField, ReadOnlyMetadataManager metadataManager)
            throws StructValidationException {
        structField.setMasked(fieldType.isMasked());

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
        return defaultValue != null && !defaultValue.isNull();
    }

    public boolean isRequired() {
        return defaultValue == null;
    }

    public void validate(ReadOnlyMetadataManager metadataManager) throws StructDefValidationException {
        // Validates field type against default value
        if (defaultValue != null && !defaultValue.isNull()) {
            try {
                this.fieldType.validateCompatibility(defaultValue, metadataManager);
            } catch (TypeValidationException e) {
                throw new StructDefValidationException(e, "StructFieldDef validation failed: " + e.getMessage());
            }
        }
    }
}
