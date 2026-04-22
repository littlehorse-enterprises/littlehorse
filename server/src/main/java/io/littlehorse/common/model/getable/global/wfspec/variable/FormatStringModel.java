package io.littlehorse.common.model.getable.global.wfspec.variable;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.sdk.common.proto.VariableAssignment;
import io.littlehorse.sdk.common.proto.VariableAssignment.FormatString;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.ArrayList;
import java.util.List;

public class FormatStringModel extends LHSerializable<FormatString> {
    private VariableAssignmentModel format;
    private List<VariableAssignmentModel> args;

    public FormatStringModel() {
        args = new ArrayList<>();
    }

    public Class<FormatString> getProtoBaseClass() {
        return FormatString.class;
    }

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

    public VariableAssignmentModel getFormat() {
        return this.format;
    }

    public List<VariableAssignmentModel> getArgs() {
        return this.args;
    }

    public void setFormat(final VariableAssignmentModel format) {
        this.format = format;
    }

    public void setArgs(final List<VariableAssignmentModel> args) {
        this.args = args;
    }
}
