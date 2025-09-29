package io.littlehorse.common.model.getable.global.wfspec;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.exceptions.validation.InvalidExpressionException;
import io.littlehorse.common.model.getable.core.variable.VariableValueModel;
import io.littlehorse.common.model.getable.global.wfspec.variable.expression.BoolReturnTypeStrategy;
import io.littlehorse.common.model.getable.global.wfspec.variable.expression.BytesReturnTypeStrategy;
import io.littlehorse.common.model.getable.global.wfspec.variable.expression.DoubleReturnTypeStrategy;
import io.littlehorse.common.model.getable.global.wfspec.variable.expression.IntReturnTypeStrategy;
import io.littlehorse.common.model.getable.global.wfspec.variable.expression.JsonArrReturnTypeStrategy;
import io.littlehorse.common.model.getable.global.wfspec.variable.expression.JsonObjReturnTypeStrategy;
import io.littlehorse.common.model.getable.global.wfspec.variable.expression.LHTypeStrategy;
import io.littlehorse.common.model.getable.global.wfspec.variable.expression.StrReturnTypeStrategy;
import io.littlehorse.common.model.getable.global.wfspec.variable.expression.WfRunIdReturnTypeStrategy;
import io.littlehorse.sdk.common.proto.TypeDefinition;
import io.littlehorse.sdk.common.proto.VariableMutationType;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.server.streams.storeinternals.ReadOnlyMetadataManager;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.Optional;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
public class TypeDefinitionModel extends LHSerializable<TypeDefinition> {

    private boolean masked;
    private VariableType type;

    public TypeDefinitionModel() {}

    public TypeDefinitionModel(VariableType type) {
        // TODO: determine whether this should be refactored to fail when type == Struct.
        this.type = type;
        this.masked = false;
    }

    public TypeDefinitionModel(VariableType type, boolean masked) {
        this.type = type;
        this.masked = masked;
    }

    @Override
    public Class<TypeDefinition> getProtoBaseClass() {
        return TypeDefinition.class;
    }

    @Override
    public TypeDefinition.Builder toProto() {
        TypeDefinition.Builder out =
                TypeDefinition.newBuilder().setMasked(masked).setType(type);
        return out;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext ctx) {
        TypeDefinition p = (TypeDefinition) proto;
        this.masked = p.getMasked();
        this.type = p.getType();
    }

    public Optional<TypeDefinitionModel> resolveTypeAfterMutationWith(
            VariableMutationType operation, TypeDefinitionModel rhs, ReadOnlyMetadataManager manager)
            throws InvalidExpressionException {
        return getTypeStrategy().resolveOperation(manager, operation, rhs.getTypeStrategy());
    }

    public LHTypeStrategy getTypeStrategy() {
        // TODO: Support StructDefs
        switch (type) {
            case INT:
                return new IntReturnTypeStrategy();
            case DOUBLE:
                return new DoubleReturnTypeStrategy();
            case STR:
                return new StrReturnTypeStrategy();
            case BOOL:
                return new BoolReturnTypeStrategy();
            case WF_RUN_ID:
                return new WfRunIdReturnTypeStrategy();
            case BYTES:
                return new BytesReturnTypeStrategy();
            case JSON_ARR:
                return new JsonArrReturnTypeStrategy();
            case JSON_OBJ:
                return new JsonObjReturnTypeStrategy();
            case TIMESTAMP:
                return new io.littlehorse.common.model.getable.global.wfspec.variable.expression
                        .TimestampReturnTypeStrategy();
            case UNRECOGNIZED:
        }
        throw new IllegalStateException();
    }

    /**
     * Being primitive means that a variable can be used as a leaf in a json tree, and that its value
     * can be serialized to a string easily.
     */
    public boolean isPrimitive() {
        // TODO: Extend this when adding Struct and StructDef.
        switch (type) {
            case INT:
            case BOOL:
            case DOUBLE:
            case WF_RUN_ID:
            case STR:
                return true;
            case JSON_OBJ:
            case JSON_ARR:
            case BYTES:
            case UNRECOGNIZED:
        }
        return false;
    }

    public static TypeDefinitionModel fromProto(TypeDefinition proto, ExecutionContext context) {
        TypeDefinitionModel out = new TypeDefinitionModel();
        out.initFrom(proto, context);
        return out;
    }

    public boolean isJson() {
        return type == VariableType.JSON_ARR || type == VariableType.JSON_OBJ;
    }

    /**
     * Returns true if the VariableValueModel matches this type.
     */
    public boolean isCompatibleWith(VariableValueModel value) {
        // TODO: Extend this when we add StructDef's and Structs.
        return value.getType() == type;
    }

    /**
     * Returns true if the other type is compatible with this type. Note that it requires
     * exact match for now. In the future we'll support casting.
     */
    public boolean isCompatibleWith(TypeDefinitionModel other) {
        if (type == VariableType.INT || type == VariableType.DOUBLE) {
            return other.getType() == VariableType.INT || other.getType() == VariableType.DOUBLE;
        }
        return this.getType().equals(other.getType());
    }

    @Override
    public String toString() {
        // TODO: when we have Structs, print out structdefid
        String result = type.toString();
        if (masked) result += " MASKED";
        return result;
    }
}
