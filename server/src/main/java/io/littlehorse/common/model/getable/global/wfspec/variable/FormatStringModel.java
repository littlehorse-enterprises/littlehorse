package io.littlehorse.common.model.getable.global.wfspec.variable;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.exceptions.LHVarSubError;
import io.littlehorse.common.model.getable.core.variable.VariableValueModel;
import io.littlehorse.common.model.getable.core.wfrun.VariableFetcher;
import io.littlehorse.sdk.common.proto.VariableAssignment;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.common.proto.VariableAssignment.FormatString;
import io.littlehorse.server.streams.topology.core.ExecutionContext;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class FormatStringModel extends LHSerializable<FormatString> {

    private VariableAssignmentModel format;
    private List<VariableAssignmentModel> args;

    public FormatStringModel() {
        args = new ArrayList<>();
    }

    @Override
    public Class<FormatString> getProtoBaseClass() {
        return FormatString.class;
    }

    @Override
    public FormatString.Builder toProto() {
        FormatString.Builder out = FormatString.newBuilder().setFormat(format.toProto());
        for (VariableAssignmentModel arg : args) {
            out.addArgs(arg.toProto());
        }
        return out;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        FormatString p = (FormatString) proto;
        format = VariableAssignmentModel.fromProto(p.getFormat(), context);
        for (VariableAssignment arg : p.getArgsList()) {
            args.add(VariableAssignmentModel.fromProto(arg, context));
        }
    }

    public VariableValueModel evaluate(VariableFetcher fetcher) throws LHVarSubError {
        VariableValueModel formatStringVarVal = this.format.assignVariable(fetcher);
        if (formatStringVarVal.getType() != VariableType.STR) {
            throw new LHVarSubError(
                    null, "Format String template isn't a STR; it's a " + formatStringVarVal.getType());
        }

        List<Object> formatArgs = new ArrayList<>();

        // second, assign the vars
        for (VariableAssignmentModel argAssn : this.args) {
            VariableValueModel variableValue = argAssn.assignVariable(fetcher);
            formatArgs.add(variableValue.getVal());
        }

        // Finally, format the String.
        try {
            return new VariableValueModel(
                    MessageFormat.format(formatStringVarVal.getStrVal(), formatArgs.toArray(new Object[0])));
        } catch (RuntimeException e) {
            throw new LHVarSubError(e, "Error formatting variable");
        }
    }
}
