package io.littlehorse.common.model.getable.global.wfspec;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.getable.core.variable.VariableValueModel;
import io.littlehorse.sdk.common.proto.ReturnType;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.Optional;
import lombok.EqualsAndHashCode;
import lombok.Setter;

@Setter
@EqualsAndHashCode(callSuper = false)
public class ReturnTypeModel extends LHSerializable<ReturnType> {

    private TypeDefinitionModel returnType;

    public ReturnTypeModel() {}

    public ReturnTypeModel(VariableType type) {
        this.returnType = new TypeDefinitionModel(type);
    }

    public ReturnTypeModel(TypeDefinitionModel typeDef) {
        this.returnType = typeDef;
    }

    public ReturnTypeModel(Optional<TypeDefinitionModel> typeDefOption) {
        if (typeDefOption.isPresent()) {
            this.returnType = typeDefOption.get();
        }
    }

    @Override
    public Class<ReturnType> getProtoBaseClass() {
        return ReturnType.class;
    }

    @Override
    public ReturnType.Builder toProto() {
        ReturnType.Builder out = ReturnType.newBuilder();

        if (returnType != null) out.setReturnType(returnType.toProto());

        return out;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext ignored) {
        ReturnType p = (ReturnType) proto;
        if (p.hasReturnType()) {
            this.returnType = LHSerializable.fromProto(p.getReturnType(), TypeDefinitionModel.class, ignored);
        }
    }

    /**
     * Returns the output type of this ReturnTypeModel. Empty if this ReturnType is VOID.
     */
    public Optional<TypeDefinitionModel> getOutputType() {
        return Optional.ofNullable(returnType);
    }

    /**
     * Returns true if the provided value's type exactly matches this ReturnType.
     */
    public boolean isCompatibleWith(VariableValueModel value) {
        if (returnType != null) {
            return returnType.isCompatibleWith(value);
        }
        return value.isEmpty();
    }
}
