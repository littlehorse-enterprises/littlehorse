package io.littlehorse.common.model.meta;

import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.exceptions.LHVarSubError;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.model.wfrun.ThreadRun;
import io.littlehorse.common.model.wfrun.VariableValue;
import io.littlehorse.common.proto.VariableMutationPb;
import io.littlehorse.common.proto.VariableMutationPb.RhsValueCase;
import io.littlehorse.common.proto.VariableMutationPbOrBuilder;
import io.littlehorse.common.proto.VariableMutationTypePb;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class VariableMutation extends LHSerializable<VariableMutationPb> {

    public String lhsName;
    public String lhsJsonPath;
    public VariableMutationTypePb operation;

    public RhsValueCase rhsValueType;
    public VariableAssignment rhsSourceVariable;
    public VariableValue rhsLiteralValue;
    public NodeOutputSource nodeOutputSource;

    public Class<VariableMutationPb> getProtoBaseClass() {
        return VariableMutationPb.class;
    }

    public VariableMutationPb.Builder toProto() {
        VariableMutationPb.Builder out = VariableMutationPb
            .newBuilder()
            .setLhsName(lhsName)
            .setOperation(operation);

        if (lhsJsonPath != null) out.setLhsJsonPath(lhsJsonPath);

        switch (rhsValueType) {
            case LITERAL_VALUE:
                out.setLiteralValue(rhsLiteralValue.toProto());
                break;
            case SOURCE_VARIABLE:
                out.setSourceVariable(rhsSourceVariable.toProto());
                break;
            case NODE_OUTPUT:
                out.setNodeOutput(nodeOutputSource.toProto()); // just set the flag
                break;
            case RHSVALUE_NOT_SET:
            // not possible
        }

        return out;
    }

    public void initFrom(MessageOrBuilder proto) {
        VariableMutationPbOrBuilder p = (VariableMutationPbOrBuilder) proto;
        lhsName = p.getLhsName();
        if (p.hasLhsJsonPath()) lhsJsonPath = p.getLhsJsonPath();
        operation = p.getOperation();

        rhsValueType = p.getRhsValueCase();
        switch (rhsValueType) {
            case LITERAL_VALUE:
                rhsLiteralValue =
                    VariableValue.fromProto(p.getLiteralValueOrBuilder());
                break;
            case SOURCE_VARIABLE:
                rhsSourceVariable =
                    VariableAssignment.fromProto(p.getSourceVariable());
                break;
            case NODE_OUTPUT:
                nodeOutputSource =
                    NodeOutputSource.fromProto(p.getNodeOutputOrBuilder());
                break;
            case RHSVALUE_NOT_SET:
            // not possible
        }
    }

    public static VariableMutation fromProto(VariableMutationPbOrBuilder p) {
        VariableMutation out = new VariableMutation();
        out.initFrom(p);
        return out;
    }

    public VariableValue getLhsValue(
        ThreadRun thread,
        Map<String, VariableValue> txnCache
    ) throws LHVarSubError {
        return getVarValFromThreadInTxn(this.lhsName, thread, txnCache);
    }

    private VariableValue getVarValFromThreadInTxn(
        String varName,
        ThreadRun thread,
        Map<String, VariableValue> txnCache
    ) throws LHVarSubError {
        VariableValue lhsVar = txnCache.get(this.lhsName);
        if (lhsVar == null) {
            lhsVar = thread.getVariable(this.lhsName).value;
        }
        return lhsVar.getCopy();
    }

    public VariableValue getRhsValue(
        ThreadRun thread,
        Map<String, VariableValue> txnCache,
        VariableValue nodeOutput
    ) throws LHVarSubError {
        VariableValue out = null;

        if (rhsValueType == RhsValueCase.LITERAL_VALUE) {
            out = rhsLiteralValue;
        } else if (rhsValueType == RhsValueCase.SOURCE_VARIABLE) {
            out = thread.assignVariable(rhsSourceVariable, txnCache);
        } else if (rhsValueType == RhsValueCase.NODE_OUTPUT) {
            out = nodeOutput;
            if (nodeOutputSource.jsonPath != null) {
                out = out.jsonPath(nodeOutputSource.jsonPath);
            }
        } else {
            throw new RuntimeException("Unsupported RHS Value type: " + rhsValueType);
        }
        return out;
    }

    public void execute(
        ThreadRun thread,
        Map<String, VariableValue> editedVars,
        VariableValue nodeOutput
    ) throws LHVarSubError {
        VariableValue lhsVal = getLhsValue(thread, editedVars);
        VariableValue rhsVal = getRhsValue(thread, editedVars, nodeOutput);

        try {
            // NOTE Part 2: see below
            if (lhsJsonPath != null) {
                VariableValue thingToPut = lhsVal
                    .jsonPath(lhsJsonPath)
                    .operate(operation, rhsVal);

                VariableValue currentLhs = getVarValFromThreadInTxn(
                    lhsName,
                    thread,
                    editedVars
                );

                currentLhs.updateJsonViaJsonPath(lhsJsonPath, thingToPut.getVal());
                editedVars.put(lhsName, currentLhs);
            } else {
                editedVars.put(lhsName, lhsVal.operate(operation, rhsVal));
            }
        } catch (LHVarSubError exn) {
            throw exn;
        } catch (Exception exn) {
            exn.printStackTrace();
            throw new LHVarSubError(
                exn,
                "Caught unexpected error when mutating variables: " + exn.getMessage()
            );
        }
    }

    public Set<String> getRequiredVariableNames() {
        Set<String> out = new HashSet<>();
        out.add(lhsName);
        if (rhsValueType == RhsValueCase.SOURCE_VARIABLE) {
            out.addAll(rhsSourceVariable.getRequiredWfRunVarNames());
        }
        return out;
    }
}
