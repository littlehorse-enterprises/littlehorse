package io.littlehorse.common.model.getable.global.structdef;

import com.google.protobuf.Message;
import io.grpc.Status;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.getable.core.variable.VariableValueModel;
import io.littlehorse.common.model.getable.global.wfspec.TypeDefinitionModel;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.StructFieldDef;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.text.MessageFormat;
import lombok.Getter;

public class StructFieldDefModel extends LHSerializable<StructFieldDef> {

    private boolean isOptional;

    @Getter
    private TypeDefinitionModel fieldType;

    private VariableValueModel defaultValue;

    @Override
    public StructFieldDef.Builder toProto() {
        StructFieldDef.Builder out =
                StructFieldDef.newBuilder().setOptional(this.isOptional).setFieldType(this.fieldType.toProto());
        out.setDefaultValue(defaultValue.toProto());

        return out;
    }

    @Override
    public void initFrom(Message p, ExecutionContext context) throws LHSerdeException {
        StructFieldDef proto = (StructFieldDef) p;
        isOptional = proto.getOptional();
        fieldType = TypeDefinitionModel.fromProto(proto.getFieldType(), context);
        defaultValue = VariableValueModel.fromProto(proto.getDefaultValue(), context);
    }

    @Override
    public Class<StructFieldDef> getProtoBaseClass() {
        return StructFieldDef.class;
    }

    public boolean hasDefaultValue() {
        return !defaultValue.isNull();
    }

    public boolean isRequired() {
        return !isOptional;
    }

    public void validate() {
        // Validates field type against default value
        if (!defaultValue.isNull() && !this.fieldType.isCompatibleWith(defaultValue)) {
            throw new LHApiException(
                    Status.INVALID_ARGUMENT,
                    MessageFormat.format(
                            "StructFieldDef field type [{0}] is not compatible with the provided default value.",
                            this.fieldType.getType().name()));
        }
    }
}
