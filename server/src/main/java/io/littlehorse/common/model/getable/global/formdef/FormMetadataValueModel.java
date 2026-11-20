package io.littlehorse.common.model.getable.global.formdef;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.getable.core.variable.VariableValueModel;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.FormMetadataValue;
import io.littlehorse.sdk.common.proto.FormMetadataValue.ValueCase;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.Map;
import java.util.Optional;
import lombok.Getter;

@Getter
public class FormMetadataValueModel extends LHSerializable<FormMetadataValue> {

    private ValueCase type;
    private VariableValueModel literal;
    private String placeholder;

    @Override
    public FormMetadataValue.Builder toProto() {
        FormMetadataValue.Builder out = FormMetadataValue.newBuilder();
        switch (type) {
            case LITERAL:
                out.setLiteral(literal.toProto());
                break;
            case PLACEHOLDER:
                out.setPlaceholder(placeholder);
                break;
            case VALUE_NOT_SET:
        }
        return out;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeException {
        FormMetadataValue p = (FormMetadataValue) proto;
        type = p.getValueCase();
        switch (type) {
            case LITERAL:
                literal = VariableValueModel.fromProto(p.getLiteral(), context);
                break;
            case PLACEHOLDER:
                placeholder = p.getPlaceholder();
                break;
            case VALUE_NOT_SET:
        }
    }

    /**
     * Returns the literal, or the placeholder's value. Empty if the placeholder has no value.
     */
    public Optional<VariableValueModel> resolve(Map<String, VariableValueModel> placeholderValues) {
        if (type == ValueCase.LITERAL) return Optional.of(literal);
        return Optional.ofNullable(placeholderValues.get(placeholder));
    }

    @Override
    public Class<FormMetadataValue> getProtoBaseClass() {
        return FormMetadataValue.class;
    }
}
