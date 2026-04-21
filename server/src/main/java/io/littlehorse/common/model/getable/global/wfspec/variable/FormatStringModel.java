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

    @Override
    public String toString() {
        return "FormatStringModel(format=" + this.getFormat() + ", args=" + this.getArgs() + ")";
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof FormatStringModel)) return false;
        final FormatStringModel other = (FormatStringModel) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$format = this.getFormat();
        final Object other$format = other.getFormat();
        if (this$format == null ? other$format != null : !this$format.equals(other$format)) return false;
        final Object this$args = this.getArgs();
        final Object other$args = other.getArgs();
        if (this$args == null ? other$args != null : !this$args.equals(other$args)) return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof FormatStringModel;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $format = this.getFormat();
        result = result * PRIME + ($format == null ? 43 : $format.hashCode());
        final Object $args = this.getArgs();
        result = result * PRIME + ($args == null ? 43 : $args.hashCode());
        return result;
    }
}
