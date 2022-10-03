package io.littlehorse.common.model.meta;

import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.model.wfrun.VariableValue;
import io.littlehorse.common.proto.VariableAssignmentPb;
import io.littlehorse.common.proto.VariableAssignmentPb.SourceCase;
import io.littlehorse.common.proto.VariableAssignmentPbOrBuilder;
import java.util.HashSet;
import java.util.Set;

public class VariableAssignment extends LHSerializable<VariableAssignmentPb> {

    public String jsonPath;
    public VariableValue defaultValue;

    public SourceCase rhsSourceType;
    public String rhsVariableName;
    public VariableValue rhsLiteralValue;

    public Class<VariableAssignmentPb> getProtoBaseClass() {
        return VariableAssignmentPb.class;
    }

    public void initFrom(MessageOrBuilder proto) {
        VariableAssignmentPbOrBuilder p = (VariableAssignmentPbOrBuilder) proto;
        if (p.hasJsonPath()) jsonPath = p.getJsonPath();
        if (p.hasDefaultValue()) {
            defaultValue = VariableValue.fromProto(p.getDefaultValue());
        }

        rhsSourceType = p.getSourceCase();
        switch (rhsSourceType) {
            case VARIABLE_NAME:
                rhsVariableName = p.getVariableName();
                break;
            case LITERAL_VALUE:
                rhsLiteralValue = VariableValue.fromProto(p.getLiteralValue());
                break;
            case SOURCE_NOT_SET:
            // nothing to do;
        }
    }

    public VariableAssignmentPb.Builder toProto() {
        VariableAssignmentPb.Builder out = VariableAssignmentPb.newBuilder();

        if (jsonPath != null) out.setJsonPath(jsonPath);
        if (defaultValue != null) out.setDefaultValue(defaultValue.toProto());

        switch (rhsSourceType) {
            case VARIABLE_NAME:
                out.setVariableName(rhsVariableName);
                break;
            case LITERAL_VALUE:
                out.setLiteralValue(rhsLiteralValue.toProto());
                break;
            case SOURCE_NOT_SET:
            // not possible.
        }

        return out;
    }

    public static VariableAssignment fromProto(VariableAssignmentPbOrBuilder proto) {
        VariableAssignment out = new VariableAssignment();
        out.initFrom(proto);
        return out;
    }

    public Set<String> getRequiredVariableNames() {
        Set<String> out = new HashSet<>();
        if (rhsSourceType == SourceCase.VARIABLE_NAME) {
            out.add(rhsVariableName);
        }
        return out;
    }
}
