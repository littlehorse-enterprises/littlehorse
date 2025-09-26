package io.littlehorse.common.model.getable.global.wfspec.variable.expression;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.exceptions.LHVarSubError;
import io.littlehorse.common.exceptions.validation.InvalidExpressionException;
import io.littlehorse.common.model.getable.core.variable.VariableValueModel;
import io.littlehorse.common.model.getable.core.wfrun.VariableAssignerFunc;
import io.littlehorse.common.model.getable.global.wfspec.TypeDefinitionModel;
import io.littlehorse.common.model.getable.global.wfspec.WfSpecModel;
import io.littlehorse.common.model.getable.global.wfspec.variable.VariableAssignmentModel;
import io.littlehorse.sdk.common.proto.VariableAssignment.Expression;
import io.littlehorse.sdk.common.proto.VariableMutationType;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.server.streams.storeinternals.ReadOnlyMetadataManager;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.Optional;
import lombok.Getter;

@Getter
public class ExpressionModel extends LHSerializable<Expression> {

    private VariableAssignmentModel lhs;
    private VariableAssignmentModel rhs;
    private VariableMutationType operation;

    @Override
    public Class<Expression> getProtoBaseClass() {
        return Expression.class;
    }

    @Override
    public Expression.Builder toProto() {
        return Expression.newBuilder()
                .setOperation(operation)
                .setLhs(lhs.toProto())
                .setRhs(rhs.toProto());
    }

    @Override
    public void initFrom(Message proto, ExecutionContext ignored) {
        Expression p = (Expression) proto;
        lhs = LHSerializable.fromProto(p.getLhs(), VariableAssignmentModel.class, ignored);
        rhs = LHSerializable.fromProto(p.getRhs(), VariableAssignmentModel.class, ignored);
        operation = p.getOperation();
    }

    public Optional<TypeDefinitionModel> resolveTypeDefinition(
            ReadOnlyMetadataManager manager, WfSpecModel wfSpec, String threadSpecName)
            throws InvalidExpressionException {

        Optional<TypeDefinitionModel> lhsTypeOption = lhs.resolveType(manager, wfSpec, threadSpecName);
        Optional<TypeDefinitionModel> rhsTypeOption = rhs.resolveType(manager, wfSpec, threadSpecName);

        if (lhsTypeOption.isEmpty() || rhsTypeOption.isEmpty()) {
            return Optional.empty();
        }

        TypeDefinitionModel lhsType = lhsTypeOption.get();
        TypeDefinitionModel rhsType = rhsTypeOption.get();

        return lhsType.resolveTypeAfterMutationWith(operation, rhsType, manager);
    }

    public VariableValueModel evaluate(VariableAssignerFunc variableFinder) throws LHVarSubError {
        VariableValueModel lhsVal = variableFinder.assign(lhs);
        VariableValueModel rhsVal = variableFinder.assign(rhs);

        TypeDefinitionModel typeToCoerceTo = lhsVal.getTypeDefinition();

        if (lhsVal.getTypeDefinition().getPrimitiveType() == VariableType.INT
                && rhsVal.getTypeDefinition().getPrimitiveType() == VariableType.DOUBLE) {
            typeToCoerceTo = new TypeDefinitionModel(VariableType.DOUBLE);
        }

        return lhsVal.operate(operation, rhsVal, typeToCoerceTo);
    }
}
