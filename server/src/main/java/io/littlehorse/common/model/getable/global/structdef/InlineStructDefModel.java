package io.littlehorse.common.model.getable.global.structdef;

import com.google.protobuf.Message;
import io.grpc.Status;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.getable.core.variable.InlineStructModel;
import io.littlehorse.common.model.getable.core.variable.StructFieldModel;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.InlineStructDef;
import io.littlehorse.sdk.common.proto.StructFieldDef;
import io.littlehorse.server.streams.storeinternals.ReadOnlyMetadataManager;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import lombok.Getter;

public class InlineStructDefModel extends LHSerializable<InlineStructDef> {

    @Getter
    private Map<String, StructFieldDefModel> fields = new HashMap<>();

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

    public void validate(ReadOnlyMetadataManager metadataManager) {
        for (Entry<String, StructFieldDefModel> field : fields.entrySet()) {
            try {
                validateStructDefFieldName(field.getKey());
            } catch (InvalidStructDefFieldNameException e) {
                throw new LHApiException(
                        Status.INVALID_ARGUMENT,
                        MessageFormat.format("StructDef Field name [{0}] invalid: " + e.getMessage(), field.getKey()));
            }

            field.getValue().validate(metadataManager);
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

    public void validateAgainst(InlineStructModel inlineStruct, ReadOnlyMetadataManager metadataManager)
            throws StructValidationException {
        for (Entry<String, StructFieldDefModel> entry : this.fields.entrySet()) {
            // If InlineStruct is missing required field...
            String fieldName = entry.getKey();
            StructFieldDefModel fieldDef = entry.getValue();

            if (fieldDef.isRequired() && !inlineStruct.getFields().containsKey(fieldName)) {
                throw new StructValidationException("Missing required field %s".formatted(fieldName));
            } else if (inlineStruct.getFields().containsKey(fieldName)) {
                StructFieldModel fieldValue = inlineStruct.getFields().get(fieldName);

                try {
                    fieldDef.validateAgainst(fieldValue, metadataManager);
                } catch (StructValidationException e) {
                    throw new StructValidationException(
                            String.format("Field '%s' is invalid: %s", fieldName, e.getMessage()));
                }
            }
        }

        for (Entry<String, StructFieldModel> entry : inlineStruct.getFields().entrySet()) {
            // If InlineStruct has extra fields...
            String fieldName = entry.getKey();

            if (!this.fields.containsKey(fieldName)) {
                throw new StructValidationException(
                        "Struct does not match StructDef, includes unrecognized field %s".formatted(fieldName));
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
