package io.littlehorse.common.model.meta;

import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.model.wfrun.VariableValue;
import io.littlehorse.common.proto.VariableMutationPb;
import io.littlehorse.common.proto.VariableMutationPb.RhsValueCase;
import io.littlehorse.common.proto.VariableMutationPbOrBuilder;
import io.littlehorse.common.proto.VariableMutationTypePb;

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
}
