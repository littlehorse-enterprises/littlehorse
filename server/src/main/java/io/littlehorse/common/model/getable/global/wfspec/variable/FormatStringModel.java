package io.littlehorse.common.model.getable.global.wfspec.variable;

import com.google.protobuf.Message;

import io.littlehorse.common.LHSerializable;
import io.littlehorse.sdk.common.proto.VariableAssignment;
import io.littlehorse.sdk.common.proto.VariableAssignment.FormatString;
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

    public void initFrom(Message proto) {
        FormatString p = (FormatString) proto;
        format = VariableAssignmentModel.fromProto(p.getFormat());
        for (VariableAssignment arg : p.getArgsList()) {
            args.add(VariableAssignmentModel.fromProto(arg));
        }
    }
}
