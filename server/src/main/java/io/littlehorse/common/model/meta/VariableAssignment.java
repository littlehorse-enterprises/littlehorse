package io.littlehorse.common.model.meta;

import com.google.protobuf.Message;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.model.wfrun.VariableValue;
import io.littlehorse.sdk.common.proto.VariableAssignmentPb;
import io.littlehorse.sdk.common.proto.VariableAssignmentPb.SourceCase;
import io.littlehorse.sdk.common.proto.VariableTypePb;
import java.util.HashSet;
import java.util.Set;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class VariableAssignment extends LHSerializable<VariableAssignmentPb> {

    private String jsonPath;
    private SourceCase rhsSourceType;
    private String variableName;
    private VariableValue rhsLiteralValue;
    private FormatString formatString;

    public Class<VariableAssignmentPb> getProtoBaseClass() {
        return VariableAssignmentPb.class;
    }

    public void initFrom(Message proto) {
        VariableAssignmentPb p = (VariableAssignmentPb) proto;
        if (p.hasJsonPath()) jsonPath = p.getJsonPath();

        rhsSourceType = p.getSourceCase();
        switch (rhsSourceType) {
            case VARIABLE_NAME:
                variableName = p.getVariableName();
                break;
            case LITERAL_VALUE:
                rhsLiteralValue = VariableValue.fromProto(p.getLiteralValue());
                break;
            case FORMAT_STRING:
                formatString =
                    LHSerializable.fromProto(p.getFormatString(), FormatString.class);
                break;
            case SOURCE_NOT_SET:
            // nothing to do;
        }
    }

    public VariableAssignmentPb.Builder toProto() {
        VariableAssignmentPb.Builder out = VariableAssignmentPb.newBuilder();

        if (jsonPath != null) out.setJsonPath(jsonPath);

        switch (rhsSourceType) {
            case VARIABLE_NAME:
                out.setVariableName(variableName);
                break;
            case LITERAL_VALUE:
                out.setLiteralValue(rhsLiteralValue.toProto());
                break;
            case FORMAT_STRING:
                out.setFormatString(formatString.toProto());
                break;
            case SOURCE_NOT_SET:
            // not possible.
        }

        return out;
    }

    public static VariableAssignment fromProto(VariableAssignmentPb proto) {
        VariableAssignment out = new VariableAssignment();
        out.initFrom(proto);
        return out;
    }

    public Set<String> getRequiredWfRunVarNames() {
        Set<String> out = new HashSet<>();
        if (rhsSourceType == SourceCase.VARIABLE_NAME) {
            out.add(variableName);
        }
        if (rhsSourceType == SourceCase.FORMAT_STRING) {
            out.addAll(formatString.getFormat().getRequiredWfRunVarNames());
            for (VariableAssignment arg : formatString.getArgs()) {
                out.addAll(arg.getRequiredWfRunVarNames());
            }
        }
        return out;
    }

    public boolean canBeType(VariableTypePb type, ThreadSpec tspec) {
        if (jsonPath != null) return true;

        VariableTypePb baseType;

        if (rhsSourceType == SourceCase.VARIABLE_NAME) {
            VariableDef varDef = tspec.getVarDef(variableName);
            baseType = varDef.type;
        } else if (rhsSourceType == SourceCase.LITERAL_VALUE) {
            baseType = rhsLiteralValue.type;
        } else {
            throw new RuntimeException("impossible");
        }

        return baseType == type;
    }
}
