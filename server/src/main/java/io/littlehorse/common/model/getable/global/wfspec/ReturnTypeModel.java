package io.littlehorse.common.model.getable.global.wfspec;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.exceptions.validation.TypeValidationException;
import io.littlehorse.common.model.getable.core.variable.VariableValueModel;
import io.littlehorse.sdk.common.proto.ReturnType;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.server.streams.storeinternals.ReadOnlyMetadataManager;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.Optional;

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
     * Returns true if the provided value's type is compatible with this ReturnType without casting.
     */
    public void validateCompatibility(VariableValueModel value, ReadOnlyMetadataManager metadataManager)
            throws TypeValidationException {
        if (returnType == null) {
            if (!value.isEmpty()) {
                throw new TypeValidationException("Expected void return type, but value is not empty.");
            }
            return;
        }
        returnType.validateCompatibility(value, metadataManager);
    }

    public void setReturnType(final TypeDefinitionModel returnType) {
        this.returnType = returnType;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof ReturnTypeModel)) return false;
        final ReturnTypeModel other = (ReturnTypeModel) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$returnType = this.returnType;
        final Object other$returnType = other.returnType;
        if (this$returnType == null ? other$returnType != null : !this$returnType.equals(other$returnType))
            return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof ReturnTypeModel;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $returnType = this.returnType;
        result = result * PRIME + ($returnType == null ? 43 : $returnType.hashCode());
        return result;
    }
}
