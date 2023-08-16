package io.littlehorse.common.model.meta;

import com.google.protobuf.Message;
import io.littlehorse.common.exceptions.LHVarSubError;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.model.wfrun.ThreadRunModel;
import io.littlehorse.common.model.wfrun.VariableValueModel;
import io.littlehorse.sdk.common.proto.VariableMutationPb;
import io.littlehorse.sdk.common.proto.VariableMutationPb.RhsValueCase;
import io.littlehorse.sdk.common.proto.VariableMutationTypePb;
import io.littlehorse.sdk.common.proto.VariableType;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class VariableMutation extends LHSerializable<VariableMutationPb> {

    public String lhsName;
    public String lhsJsonPath;
    public VariableMutationTypePb operation;

    public RhsValueCase rhsValueType;
    public VariableAssignmentModel rhsSourceVariable;
    public VariableValueModel rhsLiteralValue;
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

    public void initFrom(Message proto) {
        VariableMutationPb p = (VariableMutationPb) proto;
        lhsName = p.getLhsName();
        if (p.hasLhsJsonPath()) lhsJsonPath = p.getLhsJsonPath();
        operation = p.getOperation();

        rhsValueType = p.getRhsValueCase();
        switch (rhsValueType) {
            case LITERAL_VALUE:
                rhsLiteralValue = VariableValueModel.fromProto(p.getLiteralValue());
                break;
            case SOURCE_VARIABLE:
                rhsSourceVariable =
                    VariableAssignmentModel.fromProto(p.getSourceVariable());
                break;
            case NODE_OUTPUT:
                nodeOutputSource = NodeOutputSource.fromProto(p.getNodeOutput());
                break;
            case RHSVALUE_NOT_SET:
            // not possible
        }
    }

    public static VariableMutation fromProto(VariableMutationPb p) {
        VariableMutation out = new VariableMutation();
        out.initFrom(p);
        return out;
    }

    public VariableValueModel getLhsValue(
        ThreadRunModel thread,
        Map<String, VariableValueModel> txnCache
    ) throws LHVarSubError {
        return getVarValFromThreadInTxn(this.lhsName, thread, txnCache);
    }

    private VariableValueModel getVarValFromThreadInTxn(
        String varName,
        ThreadRunModel thread,
        Map<String, VariableValueModel> txnCache
    ) throws LHVarSubError {
        VariableValueModel lhsVar = txnCache.get(this.lhsName);
        if (lhsVar == null) {
            lhsVar = thread.getVariable(this.lhsName).value;
        }
        return lhsVar.getCopy();
    }

    public VariableValueModel getRhsValue(
        ThreadRunModel thread,
        Map<String, VariableValueModel> txnCache,
        VariableValueModel nodeOutput
    ) throws LHVarSubError {
        VariableValueModel out = null;

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
        ThreadRunModel thread,
        Map<String, VariableValueModel> txnCache,
        VariableValueModel nodeOutput
    ) throws LHVarSubError {
        VariableValueModel lhsVal = getLhsValue(thread, txnCache);
        VariableValueModel rhsVal = getRhsValue(thread, txnCache, nodeOutput);
        VariableType lhsRealType = thread
            .getThreadSpecModel()
            .getVarDef(lhsName)
            .type;

        try {
            // NOTE Part 2: see below
            if (lhsJsonPath != null) {
                VariableValueModel lhsJsonPathed = lhsVal.jsonPath(lhsJsonPath);
                VariableType typeToCoerceTo = lhsJsonPathed.type;
                VariableValueModel thingToPut = lhsJsonPathed.operate(
                    operation,
                    rhsVal,
                    typeToCoerceTo
                );

                VariableValueModel currentLhs = getVarValFromThreadInTxn(
                    lhsName,
                    thread,
                    txnCache
                );

                currentLhs.updateJsonViaJsonPath(lhsJsonPath, thingToPut.getVal());
                txnCache.put(lhsName, currentLhs);
            } else {
                VariableType typeToCoerceTo = lhsRealType;
                txnCache.put(
                    lhsName,
                    lhsVal.operate(operation, rhsVal, typeToCoerceTo)
                );
            }
        } catch (LHVarSubError exn) {
            throw exn;
        } catch (Exception exn) {
            log.error(exn.getMessage(), exn);
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
