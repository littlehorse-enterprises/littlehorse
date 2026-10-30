package io.littlehorse.common.model.getable.global.structdef;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.getable.core.variable.InlineStructModel;
import io.littlehorse.sdk.common.proto.StructOneOf;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.Map;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(callSuper = false)
public class StructOneOfModel extends LHSerializable<StructOneOf> {
    private boolean selectionRequired;

    @Override
    public StructOneOf.Builder toProto() {
        return StructOneOf.newBuilder().setSelectionRequired(selectionRequired);
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        selectionRequired = ((StructOneOf) proto).getSelectionRequired();
    }

    @Override
    public Class<StructOneOf> getProtoBaseClass() {
        return StructOneOf.class;
    }

    public void validateDefinition(Map<String, StructFieldDefModel> fields) throws StructDefValidationException {
        if (fields.isEmpty()) {
            throw new StructDefValidationException("A oneof must contain at least one alternative");
        }
        for (var field : fields.entrySet()) {
            if (field.getValue().hasDefaultValue()) {
                throw new StructDefValidationException(
                        "Oneof alternative '%s' cannot be nullable or have a default".formatted(field.getKey()));
            }
        }
    }

    public void validateSelection(InlineStructModel value, Map<String, StructFieldDefModel> fields)
            throws StructValidationException {
        for (var field : value.getFields().entrySet()) {
            if (!fields.containsKey(field.getKey())) {
                throw new StructValidationException("Unknown oneof alternative '%s'".formatted(field.getKey()));
            }
            if (field.getValue().getValue().isNull()) {
                throw new StructValidationException(
                        "Oneof alternative '%s' cannot be null; omit unused alternatives".formatted(field.getKey()));
            }
        }
        if (value.getFields().size() > 1) {
            throw new StructValidationException("Only one oneof alternative may be populated: "
                    + value.getFields().keySet());
        }
        if (selectionRequired && value.getFields().isEmpty()) {
            throw new StructValidationException("A oneof selection is required");
        }
    }
}
