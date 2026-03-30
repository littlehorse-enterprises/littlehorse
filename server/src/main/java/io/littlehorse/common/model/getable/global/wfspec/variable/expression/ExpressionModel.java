package io.littlehorse.common.model.getable.global.wfspec.variable.expression;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.exceptions.LHVarSubError;
import io.littlehorse.common.exceptions.validation.InvalidEdgeException;
import io.littlehorse.common.exceptions.validation.InvalidExpressionException;
import io.littlehorse.common.model.getable.core.variable.VariableValueModel;
import io.littlehorse.common.model.getable.core.wfrun.ThreadRunModel;
import io.littlehorse.common.model.getable.core.wfrun.VariableAssignerFunc;
import io.littlehorse.common.model.getable.global.wfspec.TypeDefinitionModel;
import io.littlehorse.common.model.getable.global.wfspec.WfSpecModel;
import io.littlehorse.common.model.getable.global.wfspec.node.Comparer;
import io.littlehorse.common.model.getable.global.wfspec.node.NodeModel;
import io.littlehorse.common.model.getable.global.wfspec.thread.ThreadSpecModel;
import io.littlehorse.common.model.getable.global.wfspec.variable.VariableAssignmentModel;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.Comparator;
import io.littlehorse.sdk.common.proto.TypeDefinition;
import io.littlehorse.sdk.common.proto.VariableAssignment.Expression;
import io.littlehorse.sdk.common.proto.VariableMutationType;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.server.streams.storeinternals.MetadataManager;
import io.littlehorse.server.streams.storeinternals.ReadOnlyMetadataManager;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.Optional;
import lombok.Getter;

@Getter
public class ExpressionModel extends LHSerializable<Expression> {

    private VariableAssignmentModel lhs;
    private VariableAssignmentModel rhs;
    private Comparator mutateByComparison;
    private VariableMutationType mutateWithOperation;

    @Override
    public Class<Expression> getProtoBaseClass() {
        return Expression.class;
    }

    @Override
    public Expression.Builder toProto() {
        Expression.Builder out = Expression.newBuilder().setLhs(lhs.toProto()).setRhs(rhs.toProto());
        if (mutateWithOperation != null) {
            out.setMutationType(mutateWithOperation);
        } else {
            out.setComparator(mutateByComparison);
        }
        return out;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext ignored) {
        Expression p = (Expression) proto;
        lhs = LHSerializable.fromProto(p.getLhs(), VariableAssignmentModel.class, ignored);
        rhs = LHSerializable.fromProto(p.getRhs(), VariableAssignmentModel.class, ignored);
        if (p.hasComparator()) {
            mutateByComparison = p.getComparator();
        }
        if (p.hasMutationType()) {
            mutateWithOperation = p.getMutationType();
        }
    }

    public boolean isAConditional() {
        return mutateByComparison != null;
    }

    public Optional<TypeDefinitionModel> resolveTypeDefinition(
            ReadOnlyMetadataManager manager, WfSpecModel wfSpec, String threadSpecName)
            throws InvalidExpressionException {
        if (isAConditional()) {
            // BOOL is the only type that can be returned from a conditional expression.
            return Optional.of(new TypeDefinitionModel(VariableType.BOOL));
        } else {
            Optional<TypeDefinitionModel> lhsTypeOption = lhs.resolveType(manager, wfSpec, threadSpecName);
            Optional<TypeDefinitionModel> rhsTypeOption = rhs.resolveType(manager, wfSpec, threadSpecName);

            if (lhsTypeOption.isEmpty() || rhsTypeOption.isEmpty()) {
                return Optional.empty();
            }

            TypeDefinitionModel lhsType = lhsTypeOption.get();
            TypeDefinitionModel rhsType = rhsTypeOption.get();
            return lhsType.resolveTypeAfterMutationWith(mutateWithOperation, rhsType, manager);
        }
    }

    public VariableValueModel evaluate(ThreadRunModel threadRun, VariableAssignerFunc variableFinder)
            throws LHVarSubError {
        if (mutateWithOperation != null) {
            return evaluateMutationWithOperation(variableFinder);
        } else {
            return new VariableValueModel(isSatisfied(threadRun));
        }
    }

    private VariableValueModel evaluateMutationWithOperation(VariableAssignerFunc variableFinder) throws LHVarSubError {
        VariableValueModel lhsVal = variableFinder.assign(lhs);
        VariableValueModel rhsVal = variableFinder.assign(rhs);

        TypeDefinitionModel typeToCoerceTo = lhsVal.getTypeDefinition();

        if (lhsVal.getTypeDefinition().getPrimitiveType() == VariableType.INT
                && rhsVal.getTypeDefinition().getPrimitiveType() == VariableType.DOUBLE) {
            typeToCoerceTo = new TypeDefinitionModel(VariableType.DOUBLE);
        }

        return lhsVal.operate(mutateWithOperation, rhsVal, typeToCoerceTo);
    }

    public void validate(NodeModel source, MetadataManager manager, ThreadSpecModel threadSpec)
            throws InvalidEdgeException {
        if (mutateByComparison == null) return;
        // TODO (#1458): after we support using VariableAssignment, make sure that the
        // resolveType() is BOOL.

        Optional<TypeDefinitionModel> lhsTypeOptional = Optional.empty();
        Optional<TypeDefinitionModel> rhsTypeOptional = Optional.empty();

        try {
            lhsTypeOptional = lhs.resolveType(manager, threadSpec.getWfSpec(), threadSpec.getName());
            rhsTypeOptional = rhs.resolveType(manager, threadSpec.getWfSpec(), threadSpec.getName());
        } catch (InvalidExpressionException e) {
            throw new InvalidEdgeException(
                    "Unable to resolve type of VariableAssignment:" + e.getMessage(), source.getName());
        }

        // Could be JSON_OBJ or JSON_ARR internal value that we can't refer the type of
        if (lhsTypeOptional.isEmpty() || rhsTypeOptional.isEmpty()) return;

        TypeDefinitionModel lhsType = lhsTypeOptional.get();
        TypeDefinitionModel rhsType = rhsTypeOptional.get();

        Optional<String> errorMessage = checkTypeComparisonIncompatibility(lhsType, mutateByComparison, rhsType);

        if (errorMessage.isPresent()) {
            throw new InvalidEdgeException(errorMessage.get(), source.getName());
        }
    }

    public static Optional<String> checkTypeComparisonIncompatibility(
            TypeDefinitionModel lhsType, Comparator comparator, TypeDefinitionModel rhsType) {
        LHUtil.LHComparisonRule rule = LHUtil.getRuleFromComparator(comparator);

        // All types can be compared agaisnt NULL
        if (rhsType.getDefinedTypeCase() == TypeDefinition.DefinedTypeCase.DEFINEDTYPE_NOT_SET) {
            return Optional.empty();
        }

        if (!rhsType.getComparisonRules().contains(rule)) {
            return Optional.of(
                    String.format("You cannot compare RHS type %s using Comparator %s", rhsType, comparator));
        }
        if (rule == LHUtil.LHComparisonRule.IDENTITY) {
            boolean typesEqual = lhsType.equals(rhsType);

            boolean lhsTypeIsComparable = lhsType.getComparisonRules().contains(LHUtil.LHComparisonRule.MAGNITUDE);
            boolean rhsTypeIsComparable = rhsType.getComparisonRules().contains(LHUtil.LHComparisonRule.MAGNITUDE);

            if (!typesEqual && (!lhsTypeIsComparable || !rhsTypeIsComparable)) {
                return Optional.of(String.format(
                        "You can only compare LHS type %s with its own type, but tried to compare it to %s",
                        lhsType, rhsType));
            }
        }

        if (rule == LHUtil.LHComparisonRule.INCLUDES) {
            boolean rhsSupportsIncludes = rhsType.getComparisonRules().contains(LHUtil.LHComparisonRule.INCLUDES);

            if (!rhsSupportsIncludes) {
                return Optional.of(String.format("You cannot use LHS type %s with Comparator %s", lhsType, comparator));
            }

            boolean isJsonArr = (rhsType.getDefinedTypeCase() == TypeDefinition.DefinedTypeCase.PRIMITIVE_TYPE
                    && rhsType.getPrimitiveType() == VariableType.JSON_ARR);
            boolean lhsIsString = (lhsType.getDefinedTypeCase() == TypeDefinition.DefinedTypeCase.PRIMITIVE_TYPE
                    && lhsType.getPrimitiveType() == VariableType.STR);

            if (!isJsonArr && !lhsIsString) {
                return Optional.of(String.format("You cannot use LHS type %s with Comparator %s", lhsType, comparator));
            }
        }
        return Optional.empty();
    }

    /**
     * Given a ThreadRunModel representing a ThreadRun in a WfRun, returns true if
     * the represented EdgeCondition is satisfied by the variables in that
     * ThreadRun.
     *
     * @param threadRun is the ThreadRunModel representing the ThreadRun to evaluate
     *                  against.
     * @return true if the condition is satisfied.
     * @throws LHVarSubError if there is a problem getting variables.
     */
    public boolean isSatisfied(ThreadRunModel threadRun) throws LHVarSubError {

        VariableValueModel lhs = threadRun.assignVariable(this.lhs);
        VariableValueModel rhs = threadRun.assignVariable(this.rhs);

        if (mutateWithOperation != null) {
            return switch (this.mutateWithOperation) {
                case AND -> lhs != null && (lhs.getBoolVal() && rhs.getBoolVal());
                case OR -> lhs != null && (lhs.getBoolVal() || rhs.getBoolVal());
                default -> throw new IllegalStateException(
                        "Unknown value for VariableMutationType enum %d".formatted(mutateWithOperation.getNumber()));
            };
        } else {
            switch (this.mutateByComparison) {
                case LESS_THAN:
                    return Comparer.compare(lhs, rhs) < 0;
                case LESS_THAN_EQ:
                    return Comparer.compare(lhs, rhs) <= 0;
                case GREATER_THAN:
                    return Comparer.compare(lhs, rhs) > 0;
                case GREATER_THAN_EQ:
                    return Comparer.compare(lhs, rhs) >= 0;
                case EQUALS:
                    return lhs != null && Comparer.compare(lhs, rhs) == 0;
                case NOT_EQUALS:
                    return lhs != null && Comparer.compare(lhs, rhs) != 0;
                case IN:
                    return Comparer.contains(rhs, lhs);
                case NOT_IN:
                    return !Comparer.contains(rhs, lhs);
                case UNRECOGNIZED:
            }

            // This is impossible; it means that we added a new value to the Comparator
            // proto
            // without updating the LH Server to handle it. So we can throw an
            // IllegalStateException.
            throw new IllegalStateException(
                    "Unknown value for Comparator enum %d".formatted(mutateByComparison.getNumber()));
        }
    }

    public boolean isConditional() {
        return (mutateByComparison != null)
                || (mutateWithOperation != null
                        && (mutateWithOperation == VariableMutationType.AND
                                || mutateWithOperation == VariableMutationType.OR));
    }
}
