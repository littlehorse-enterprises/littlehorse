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
            // TODO: Propose and agree upon Field Name validation technique!
            // if (!LHUtil.isValidLHName(field.getKey())) {
            //     throw new LHApiException(
            //             Status.INVALID_ARGUMENT,
            //             MessageFormat.format("StructField name [{0}] must be a valid hostname", field.getKey()));
            // }

            field.getValue().validate(metadataManager);
        }
    }

    public boolean validateAgainst(InlineStructModel inlineStruct, ReadOnlyMetadataManager metadataManager) {
        for (Entry<String, StructFieldDefModel> entry : this.fields.entrySet()) {
            // If InlineStruct is missing required field...
            String fieldName = entry.getKey();
            StructFieldDefModel fieldDef = entry.getValue();

            if (fieldDef.isRequired() && !inlineStruct.getFields().containsKey(fieldName)) {
                throw new LHApiException(
                        Status.INVALID_ARGUMENT,
                        "Struct does not match StructDef, missing required field %s".formatted(fieldName));
            } else if (inlineStruct.getFields().containsKey(fieldName)) {
                StructFieldModel fieldValue = inlineStruct.getFields().get(fieldName);

                fieldDef.validateAgainst(fieldValue, metadataManager);
            }
        }

        return false;
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
