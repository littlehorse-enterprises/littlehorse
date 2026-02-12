package io.littlehorse.common.model.getable.global.wfspec.variable;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.exceptions.LHVarSubError;
import io.littlehorse.common.exceptions.validation.InvalidExpressionException;
import io.littlehorse.common.exceptions.validation.InvalidMutationException;
import io.littlehorse.common.model.getable.core.variable.VariableModel;
import io.littlehorse.common.model.getable.core.variable.VariableValueModel;
import io.littlehorse.common.model.getable.core.wfrun.ThreadRunModel;
import io.littlehorse.common.model.getable.global.wfspec.TypeDefinitionModel;
import io.littlehorse.common.model.getable.global.wfspec.node.NodeModel;
import io.littlehorse.common.model.getable.global.wfspec.thread.ThreadSpecModel;
import io.littlehorse.common.util.TypeCastingUtils;
import io.littlehorse.sdk.common.proto.Operation;
import io.littlehorse.sdk.common.proto.VariableMutation;
import io.littlehorse.sdk.common.proto.VariableMutation.RhsValueCase;
import io.littlehorse.sdk.common.proto.Operation;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.server.streams.storeinternals.ReadOnlyMetadataManager;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public class VariableMutationModel extends LHSerializable<VariableMutation> {

    private String lhsName;
    private String lhsJsonPath;
    private Operation operation;

    private RhsValueCase rhsValueType;
    private VariableAssignmentModel rhsRhsAssignment;
    private VariableValueModel rhsLiteralValue;
    private NodeOutputSourceModel nodeOutputSource;

    @Override
    public Class<VariableMutation> getProtoBaseClass() {
        return VariableMutation.class;
    }

    @Override
    public VariableMutation.Builder toProto() {
        VariableMutation.Builder out =
                VariableMutation.newBuilder().setLhsName(lhsName).setOperation(operation);

        if (lhsJsonPath != null) out.setLhsJsonPath(lhsJsonPath);

        switch (rhsValueType) {
            case LITERAL_VALUE:
                out.setLiteralValue(rhsLiteralValue.toProto());
                break;
            case RHS_ASSIGNMENT:
                out.setRhsAssignment(rhsRhsAssignment.toProto());
                break;
            case NODE_OUTPUT:
                out.setNodeOutput(nodeOutputSource.toProto()); // just set the flag
                break;
            case RHSVALUE_NOT_SET:
                // not possible
        }

        return out;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        VariableMutation p = (VariableMutation) proto;
        lhsName = p.getLhsName();
        if (p.hasLhsJsonPath()) lhsJsonPath = p.getLhsJsonPath();
        operation = p.getOperation();

        rhsValueType = p.getRhsValueCase();
        switch (rhsValueType) {
            case LITERAL_VALUE:
                rhsLiteralValue = VariableValueModel.fromProto(p.getLiteralValue(), context);
                break;
            case RHS_ASSIGNMENT:
                rhsRhsAssignment = VariableAssignmentModel.fromProto(p.getRhsAssignment(), context);
                break;
            case NODE_OUTPUT:
                nodeOutputSource = NodeOutputSourceModel.fromProto(p.getNodeOutput(), context);
                break;
            case RHSVALUE_NOT_SET:
                // not possible
        }
    }

    public static VariableMutationModel fromProto(VariableMutation p, ExecutionContext context) {
        VariableMutationModel out = new VariableMutationModel();
        out.initFrom(p, context);
        return out;
    }

    public VariableValueModel getLhsValue(ThreadRunModel thread, Map<String, VariableValueModel> txnCache)
            throws LHVarSubError {
        return getVarValFromThreadInTxn(this.lhsName, thread, txnCache);
    }

    private VariableValueModel getVarValFromThreadInTxn(
            String varName, ThreadRunModel thread, Map<String, VariableValueModel> txnCache) throws LHVarSubError {
        VariableValueModel result = txnCache.get(this.lhsName);
        if (result == null) {
            VariableModel rawVariable = thread.getVariable(varName);
            if (rawVariable == null) {
                throw new LHVarSubError(
                        null,
                        "Variable %s is out of scope for threadRun %d of spec %s. Invalid WfSpec!"
                                .formatted(varName, thread.getNumber(), thread.getThreadSpecName()));
            }
            result = rawVariable.getValue();
        }

        return result.getCopy();
    }

    public VariableValueModel getRhsValue(
            ThreadRunModel thread, Map<String, VariableValueModel> txnCache, VariableValueModel nodeOutput)
            throws LHVarSubError {
        VariableValueModel out = null;

        if (rhsValueType == RhsValueCase.LITERAL_VALUE) {
            out = rhsLiteralValue;
        } else if (rhsValueType == RhsValueCase.RHS_ASSIGNMENT) {
            out = thread.assignVariable(rhsRhsAssignment, txnCache);
        } else if (rhsValueType == RhsValueCase.NODE_OUTPUT) {
            out = nodeOutput;
            switch (nodeOutputSource.getPathCase()) {
                case JSONPATH:
                    out = out.jsonPath(nodeOutputSource.getJsonPath());
                    break;
                case LH_PATH:
                    out = out.get(nodeOutputSource.getLhPath());
                    break;
                case PATH_NOT_SET:
            }
        } else {
            throw new RuntimeException("Unsupported RHS Value type: " + rhsValueType);
        }
        return out;
    }

    public void execute(ThreadRunModel thread, Map<String, VariableValueModel> txnCache, VariableValueModel nodeOutput)
            throws LHVarSubError {
        VariableValueModel lhsVal = getLhsValue(thread, txnCache);
        VariableValueModel rhsVal = getRhsValue(thread, txnCache, nodeOutput);

        // This will need to be refactored once we introduce Structs.
        TypeDefinitionModel lhsRealType =
                thread.getThreadSpec().getVarDef(lhsName).getVarDef().getTypeDef();

        try {
            // NOTE Part 2: see below
            if (lhsJsonPath != null) {
                VariableValueModel lhsJsonPathed = lhsVal.jsonPath(lhsJsonPath);
                TypeDefinitionModel typeToCoerceTo = lhsJsonPathed.getTypeDefinition();

                // If the key does not exist in the LHS, we just plop the RHS there. Otherwise,
                // we want to coerce the
                // type to the rhs.
                VariableValueModel thingToPut =
                        lhsJsonPathed.getTypeDefinition().isNull()
                                ? rhsVal
                                : lhsJsonPathed.operate(operation, rhsVal, typeToCoerceTo);

                VariableValueModel currentLhs = getVarValFromThreadInTxn(lhsName, thread, txnCache);

                currentLhs.updateJsonViaJsonPath(lhsJsonPath, thingToPut.getVal());
                txnCache.put(lhsName, currentLhs);
            } else {
                TypeDefinitionModel typeToCoerceTo = lhsRealType;
                txnCache.put(lhsName, lhsVal.operate(operation, rhsVal, typeToCoerceTo));
            }
        } catch (LHVarSubError exn) {
            throw exn;
        } catch (Exception exn) {
            log.trace(exn.getMessage(), exn);
            throw new LHVarSubError(exn, "Caught unexpected error when mutating variables: " + exn.getMessage());
        }
    }

    public Set<String> getRequiredVariableNames() {
        Set<String> out = new HashSet<>();
        out.add(lhsName);
        if (rhsValueType == RhsValueCase.RHS_ASSIGNMENT) {
            out.addAll(rhsRhsAssignment.getRequiredWfRunVarNames());
        }
        return out;
    }

    public void validate(NodeModel source, ReadOnlyMetadataManager manager, ThreadSpecModel threadSpec)
            throws InvalidMutationException {
        if (lhsJsonPath != null) {
            // Can't validate anything, sorry.
            return;
        }

        TypeDefinitionModel lhsType = threadSpec.getVarDef(lhsName).getVarDef().getTypeDef();

        try {
            Optional<TypeDefinitionModel> rhsType =
                    rhsRhsAssignment.resolveType(manager, threadSpec.getWfSpec(), threadSpec.getName());
            if (rhsType.isEmpty()) {
                return;
            }

            if (operation == Operation.ASSIGN) {
                if (rhsValueType == RhsValueCase.RHS_ASSIGNMENT && rhsRhsAssignment.getTargetType() != null) {
                    // Step 1: Validate the explicit cast (original type -> cast target)
                    Optional<TypeDefinitionModel> sourceTypeOpt =
                            rhsRhsAssignment.getSourceType(manager, threadSpec.getWfSpec(), threadSpec.getName());
                    VariableType originalType = sourceTypeOpt
                            .map(TypeDefinitionModel::getPrimitiveType)
                            .orElse(null);
                    VariableType castTargetType = rhsRhsAssignment.getTargetType() != null
                            ? rhsRhsAssignment.getTargetType().getPrimitiveType()
                            : null;
                    if (!TypeCastingUtils.canCastTo(originalType, castTargetType)) {
                        throw new InvalidMutationException("Cannot cast from " + originalType + " to " + castTargetType
                                + ". This conversion is not supported.");
                    }
                    TypeCastingUtils.validateTypeCompatibility(originalType, castTargetType);

                    // Step 2: Validate assignment (cast target type -> lhs type)
                    TypeCastingUtils.validateTypeCompatibility(castTargetType, lhsType.getPrimitiveType());
                } else {
                    // No explicit cast, only allow assignment if possible without cast
                    VariableType rhsActualType = rhsType.get().getPrimitiveType();
                    VariableType lhsActualType = lhsType.getPrimitiveType();
                    if (!TypeCastingUtils.canBeType(rhsActualType, lhsActualType)) {
                        throw new InvalidMutationException("Cannot assign " + rhsActualType + " to " + lhsActualType
                                + " without explicit casting.");
                    }
                    TypeCastingUtils.validateTypeCompatibility(rhsActualType, lhsActualType);
                }
            }

            Optional<TypeDefinitionModel> resultingType = lhsType.getTypeStrategy()
                    .resolveOperation(manager, operation, rhsType.get().getTypeStrategy());
            if (resultingType.isPresent() && !lhsType.isCompatibleWith(resultingType.get())) {
                throw new InvalidMutationException(
                        "Cannot mutate a " + lhsType + " by assigning it a value of type " + resultingType.get());
            }
        } catch (InvalidExpressionException exn) {
            throw new InvalidMutationException("Mutation of variable " + lhsName + " invalid: " + exn.getMessage());
        }
    }
}
