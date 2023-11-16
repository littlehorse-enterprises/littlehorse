package io.littlehorse.common.model.getable.global.wfspec.variable;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.getable.core.variable.VariableValueModel;
import io.littlehorse.common.model.getable.global.wfspec.thread.ThreadSpecModel;
import io.littlehorse.sdk.common.proto.VariableAssignment;
import io.littlehorse.sdk.common.proto.VariableAssignment.SourceCase;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.HashSet;
import java.util.Set;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class VariableAssignmentModel extends LHSerializable<VariableAssignment> {

    private String jsonPath;
    private SourceCase rhsSourceType;
    private String variableName;
    private VariableValueModel rhsLiteralValue;
    private FormatStringModel formatString;

    public Class<VariableAssignment> getProtoBaseClass() {
        return VariableAssignment.class;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        VariableAssignment p = (VariableAssignment) proto;
        if (p.hasJsonPath()) jsonPath = p.getJsonPath();

        rhsSourceType = p.getSourceCase();
        switch (rhsSourceType) {
            case VARIABLE_NAME:
                variableName = p.getVariableName();
                break;
            case LITERAL_VALUE:
                rhsLiteralValue = VariableValueModel.fromProto(p.getLiteralValue(), context);
                break;
            case FORMAT_STRING:
                formatString = LHSerializable.fromProto(p.getFormatString(), FormatStringModel.class, context);
                break;
            case SOURCE_NOT_SET:
                // nothing to do;
        }
    }

    public VariableAssignment.Builder toProto() {
        VariableAssignment.Builder out = VariableAssignment.newBuilder();

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

    public static VariableAssignmentModel fromProto(VariableAssignment proto, ExecutionContext context) {
        VariableAssignmentModel out = new VariableAssignmentModel();
        out.initFrom(proto, context);
        return out;
    }

    public Set<String> getRequiredWfRunVarNames() {
        Set<String> out = new HashSet<>();
        if (rhsSourceType == SourceCase.VARIABLE_NAME) {
            out.add(variableName);
        }
        if (rhsSourceType == SourceCase.FORMAT_STRING) {
            out.addAll(formatString.getFormat().getRequiredWfRunVarNames());
            for (VariableAssignmentModel arg : formatString.getArgs()) {
                out.addAll(arg.getRequiredWfRunVarNames());
            }
        }
        return out;
    }

    public boolean canBeType(VariableType type, ThreadSpecModel tspec) {
        if (jsonPath != null) return true;

        VariableType baseType;

        if (rhsSourceType == SourceCase.VARIABLE_NAME) {
            VariableDefModel varDef = tspec.getVarDef(variableName);
            baseType = varDef.type;
        } else if (rhsSourceType == SourceCase.LITERAL_VALUE) {
            baseType = rhsLiteralValue.type;
        } else if (rhsSourceType == SourceCase.FORMAT_STRING) {
            baseType = VariableType.STR;
        } else {
            throw new RuntimeException("impossible");
        }

        return baseType == type;
    }
}
