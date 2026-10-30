package io.littlehorse.common.model.getable.global.structdef;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.exceptions.validation.TypeValidationException;
import io.littlehorse.common.model.getable.core.variable.InlineStructModel;
import io.littlehorse.common.model.getable.core.variable.StructFieldModel;
import io.littlehorse.common.model.getable.core.variable.VariableValueModel;
import io.littlehorse.common.model.getable.global.wfspec.IngressTypeUtils;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.InlineStructDef;
import io.littlehorse.sdk.common.proto.StructFieldDef;
import io.littlehorse.server.streams.storeinternals.ReadOnlyMetadataManager;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@EqualsAndHashCode(callSuper = false)
public class InlineStructDefModel extends LHSerializable<InlineStructDef> {

    @Getter
    private Map<String, StructFieldDefModel> fields = new HashMap<>();

    @Getter
    private StructOneOfModel oneofConfig;

    public InlineStructDefModel() {}

    public InlineStructDefModel(InlineStructDefModel other) {
        if (other != null) {
            if (other.oneofConfig != null) {
                oneofConfig =
                        LHSerializable.fromProto(other.oneofConfig.toProto().build(), StructOneOfModel.class, null);
            }
            other.fields.forEach((key, fieldDef) -> this.fields.put(
                    key, LHSerializable.fromProto(fieldDef.toProto().build(), StructFieldDefModel.class, null)));
        }
    }

    @Override
    public InlineStructDef.Builder toProto() {
        InlineStructDef.Builder out = InlineStructDef.newBuilder();
        if (oneofConfig != null) out.setOneofConfig(oneofConfig.toProto());

        for (Entry<String, StructFieldDefModel> field : fields.entrySet()) {
            out.putFields(field.getKey(), field.getValue().toProto().build());
        }

        return out;
    }

    @Override
    public void initFrom(Message p, ExecutionContext context) throws LHSerdeException {
        InlineStructDef proto = (InlineStructDef) p;
        oneofConfig = proto.hasOneofConfig()
                ? LHSerializable.fromProto(proto.getOneofConfig(), StructOneOfModel.class, context)
                : null;

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
        if (oneofConfig != null) oneofConfig.validateDefinition(fields);
        for (Entry<String, StructFieldDefModel> field : fields.entrySet()) {
            try {
                validateStructDefFieldName(field.getKey());
            } catch (InvalidStructDefFieldNameException e) {
                throw new StructDefValidationException(
                        e, String.format("StructDef field name '%s' invalid: %s", field.getKey(), e.getMessage()));
            }

            try {
                if (field.getValue().getFieldType() != null) {
                    field.getValue().getFieldType().validateAndPin(metadataManager);
                }
                field.getValue().validate(metadataManager);
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
        if (oneofConfig != null) oneofConfig.validateSelection(inlineStruct, fields);
        for (Entry<String, StructFieldDefModel> entry : this.fields.entrySet()) {
            String fieldName = entry.getKey();
            StructFieldDefModel fieldDef = entry.getValue();

            if (!inlineStruct.getFields().containsKey(fieldName)) {
                if (oneofConfig != null) continue;
                if (fieldDef.isRequired()) {
                    throw new StructValidationException("Missing required field %s".formatted(fieldName));
                }
                // Apply default value for absent optional fields.
                VariableValueModel defaultVal = fieldDef.getDefaultValue();
                if (defaultVal != null) {
                    StructFieldModel defaultField = new StructFieldModel();
                    VariableValueModel defaultValueCopy = defaultVal.getCopy();
                    try {
                        IngressTypeUtils.applyExpectedTypeAndValidate(
                                Optional.of(fieldDef.getFieldType()), defaultValueCopy, metadataManager);
                    } catch (TypeValidationException e) {
                        throw new StructValidationException(
                                e,
                                String.format(
                                        "Default value for field '%s' is invalid: %s", fieldName, e.getMessage()));
                    }
                    defaultField.setValue(defaultValueCopy);
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

    public void validateNamedStructDef(ReadOnlyMetadataManager metadataManager) throws StructDefValidationException {
        if (oneofConfig != null) {
            throw new StructDefValidationException("A named StructDef cannot itself be a oneof; use an inline field");
        }
        validate(metadataManager);
    }

    public static InlineStructDefModel fromProto(InlineStructDef p, ExecutionContext context) {
        InlineStructDefModel out = new InlineStructDefModel();
        out.initFrom(p, context);
        return out;
    }
}
