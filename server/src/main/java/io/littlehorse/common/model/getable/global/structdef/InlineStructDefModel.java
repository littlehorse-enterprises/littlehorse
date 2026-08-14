package io.littlehorse.common.model.getable.global.structdef;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.exceptions.UnknownStructDefException;
import io.littlehorse.common.model.getable.core.variable.InlineStructModel;
import io.littlehorse.common.model.getable.core.variable.StructFieldModel;
import io.littlehorse.common.model.getable.core.variable.VariableValueModel;
import io.littlehorse.common.model.getable.global.wfspec.TypeDefinitionModel;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.InlineStructDef;
import io.littlehorse.sdk.common.proto.StructFieldDef;
import io.littlehorse.server.streams.storeinternals.ReadOnlyMetadataManager;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@EqualsAndHashCode(callSuper = false)
public class InlineStructDefModel extends LHSerializable<InlineStructDef> {

    @Getter
    private Map<String, StructFieldDefModel> fields = new HashMap<>();

    public InlineStructDefModel() {}

    public InlineStructDefModel(InlineStructDefModel other) {
        if (other != null) {
            other.fields.forEach((key, fieldDef) -> this.fields.put(
                    key, LHSerializable.fromProto(fieldDef.toProto().build(), StructFieldDefModel.class, null)));
        }
    }

    @Override
    public InlineStructDef.Builder toProto() {
        InlineStructDef.Builder out = InlineStructDef.newBuilder();

        for (Entry<String, StructFieldDefModel> field : fields.entrySet()) {
            out.putFields(field.getKey(), field.getValue().toProto().build());
        }

        return out;
    }

    @Override
    public void initFrom(Message p, ExecutionContext context) throws LHSerdeException {
        InlineStructDef proto = (InlineStructDef) p;

        for (Entry<String, StructFieldDef> structFieldDef : proto.getFieldsMap().entrySet()) {
            fields.put(
                    structFieldDef.getKey(),
                    LHSerializable.fromProto(structFieldDef.getValue(), StructFieldDefModel.class, context));
        }
    }

    @Override
    public Class<InlineStructDef> getProtoBaseClass() {
        return InlineStructDef.class;
    }

    public void validate(ReadOnlyMetadataManager metadataManager) throws StructDefValidationException {
        for (Entry<String, StructFieldDefModel> field : fields.entrySet()) {
            try {
                validateStructDefFieldName(field.getKey());
            } catch (InvalidStructDefFieldNameException e) {
                throw new StructDefValidationException(
                        e, String.format("StructDef field name '%s' invalid: %s", field.getKey(), e.getMessage()));
            }

            try {
                // Ensure any STRUCT_DEF_ID referenced in the field's type exists and is pinned to the concrete latest
                // version
                if (field.getValue().getFieldType() != null) {
                    try {
                        field.getValue().getFieldType().validateStructDefExistsAndPinVersion(metadataManager);
                    } catch (UnknownStructDefException e) {
                        throw new StructDefValidationException(e, e.getMessage());
                    }
                }

                field.getValue().validate(metadataManager);

                // Recurse into nested inline struct defs for field-name validation
                TypeDefinitionModel fieldType = field.getValue().getFieldType();
                if (fieldType != null && fieldType.getInlineStructDef() != null) {
                    fieldType.getInlineStructDef().validate(metadataManager);
                }
            } catch (StructDefValidationException e) {
                throw new StructDefValidationException(
                        e, String.format("StructDef field '%s' invalid: %s", field.getKey(), e.getMessage()));
            }
        }
    }

    // Designed to provide detailed feedback
    public static void validateStructDefFieldName(String name) throws InvalidStructDefFieldNameException {
        if (name == null) {
            throw new InvalidStructDefFieldNameException("illegal state, names cannot be null");
        }

        if (!Character.isLetter(name.charAt(0))) {
            throw new InvalidStructDefFieldNameException("first character must be a letter");
        }

        if (Character.isUpperCase(name.charAt(0))) {
            throw new InvalidStructDefFieldNameException("first letter must be lowercase");
        }

        if (name.contains("_")) {
            throw new InvalidStructDefFieldNameException("cannot include underscores, must follow camelCase");
        }

        if (!name.matches("^[a-zA-Z0-9]+$")) {
            throw new InvalidStructDefFieldNameException("cannot include special characters, must be alphanumeric");
        }
    }

    /**
     * Superset-compatible validation: value may contain extra fields that are not
     * present in this StructDef. Required fields from the StructDef are still enforced.
     * Use this for runtime ingestion where clients may send newer fields than the
     * pinned schema.
     *
     * @param inlineStruct The InlineStruct to validate.
     * @param metadataManager Read-only metadata manager used to resolve nested struct types.
     * @throws StructValidationException if the payload is missing required fields or
     *                                   contains fields incompatible with the StructDef.
     */
    public void validateAgainstSuperset(InlineStructModel inlineStruct, ReadOnlyMetadataManager metadataManager)
            throws StructValidationException {
        for (Entry<String, StructFieldDefModel> entry : this.fields.entrySet()) {
            String fieldName = entry.getKey();
            StructFieldDefModel fieldDef = entry.getValue();

            if (!inlineStruct.getFields().containsKey(fieldName)) {
                if (fieldDef.isRequired()) {
                    throw new StructValidationException("Missing required field %s".formatted(fieldName));
                }
                // Apply default value for absent optional fields.
                VariableValueModel defaultVal = fieldDef.getDefaultValue();
                if (defaultVal != null) {
                    StructFieldModel defaultField = new StructFieldModel();
                    defaultField.setValue(defaultVal);
                    defaultField.setMasked(fieldDef.getFieldType().isMasked());
                    inlineStruct.getFields().put(fieldName, defaultField);
                }
            } else {
                StructFieldModel fieldValue = inlineStruct.getFields().get(fieldName);
                try {
                    fieldDef.validateAgainst(fieldValue, metadataManager);
                } catch (StructValidationException e) {
                    throw new StructValidationException(
                            String.format("Field '%s' is invalid: %s", fieldName, e.getMessage()));
                }
            }
        }
    }

    public Map<String, StructFieldDefModel> getRequiredFields() {
        return fields.entrySet().stream()
                .filter(entry -> entry.getValue().isRequired())
                .collect(Collectors.toMap(entry -> entry.getKey(), entry -> entry.getValue()));
    }

    public static InlineStructDefModel fromProto(InlineStructDef p, ExecutionContext context) {
        InlineStructDefModel out = new InlineStructDefModel();
        out.initFrom(p, context);
        return out;
    }
}
