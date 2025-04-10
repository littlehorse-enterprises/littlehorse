package io.littlehorse.common.model.getable.global.wfspec;

import java.util.Objects;
import java.util.Optional;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.getable.core.variable.VariableValueModel;
import io.littlehorse.sdk.common.proto.ReturnType;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import lombok.Setter;

@Setter
public class ReturnTypeModel extends LHSerializable<ReturnType> {

    private TypeDefinitionModel returnType;

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

    @Override
    public boolean equals(Object other) {
        if (other instanceof ReturnTypeModel) {
            TypeDefinitionModel otherType = ((ReturnTypeModel) other).getOutputType().orElse(null);
            return Objects.equals(otherType, returnType);
        } else {
            return false;
        }
    }

    public boolean isVoidOrPrimitive() {
        return getOutputType().isEmpty() || getOutputType().get().isPrimitive();
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
