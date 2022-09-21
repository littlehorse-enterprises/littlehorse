package io.littlehorse.common.model.meta;

import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.exceptions.LHVarSubError;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.model.event.TaskResultEvent;
import io.littlehorse.common.model.wfrun.ThreadRun;
import io.littlehorse.common.model.wfrun.VariableValue;
import io.littlehorse.common.proto.VariableMutationPb;
import io.littlehorse.common.proto.VariableMutationPb.RhsValueCase;
import io.littlehorse.common.proto.VariableMutationPbOrBuilder;
import io.littlehorse.common.proto.VariableMutationTypePb;
import java.util.Map;

public class VariableMutation extends LHSerializable<VariableMutationPb> {

    public String lhsName;
    public String lhsJsonPath;
    public VariableMutationTypePb operation;

    public RhsValueCase rhsValueType;
    public VariableAssignment rhsSourceVariable;
    public VariableValue rhsLiteralValue;
    public String rhsJsonPath;

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
                // nothing to do
                break;
            case RHSVALUE_NOT_SET:
            // not possible
        }

        if (rhsJsonPath != null) out.setRhsJsonPath(rhsJsonPath);

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
                // nothing to do
                break;
            case RHSVALUE_NOT_SET:
            // not possible
        }
        if (p.hasRhsJsonPath()) rhsJsonPath = p.getRhsJsonPath();
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
        if (lhsJsonPath != null) {
            throw new RuntimeException("JsonPath not supported yet");
        }
        return lhsVar.getCopy();
    }

    public VariableValue getRhsValue(
        ThreadRun thread,
        Map<String, VariableValue> txnCache,
        TaskResultEvent tre
    ) throws LHVarSubError {
        VariableValue out = null;

        if (rhsValueType == RhsValueCase.LITERAL_VALUE) {
            out = rhsLiteralValue;
        } else if (rhsValueType == RhsValueCase.SOURCE_VARIABLE) {
            out = thread.assignVariable(rhsSourceVariable, txnCache);
        } else if (rhsValueType == RhsValueCase.NODE_OUTPUT) {
            out = tre.stdout;
        } else {
            throw new RuntimeException(
                "Unsupported RHS Value type: " + rhsValueType
            );
        }

        if (rhsJsonPath != null) {
            throw new RuntimeException("JsonPath not yet supported");
        }
        return out;
    }

    public void execute(
        ThreadRun thread,
        Map<String, VariableValue> editedVars,
        TaskResultEvent tre
    ) throws LHVarSubError {
        VariableValue lhsVal = getLhsValue(thread, editedVars);
        VariableValue rhsVal = getRhsValue(thread, editedVars, tre);

        editedVars.put(lhsName, lhsVal.operate(operation, rhsVal));
    }
}
