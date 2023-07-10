package io.littlehorse.common.model.meta;

import com.google.protobuf.Message;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.sdk.common.proto.VariableAssignmentPb;
import io.littlehorse.sdk.common.proto.VariableAssignmentPb.FormatStringPb;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class FormatString extends LHSerializable<FormatStringPb> {

    private VariableAssignment format;
    private List<VariableAssignment> args;

    public FormatString() {
        args = new ArrayList<>();
    }

    public Class<FormatStringPb> getProtoBaseClass() {
        return FormatStringPb.class;
    }

    public FormatStringPb.Builder toProto() {
        FormatStringPb.Builder out = FormatStringPb
            .newBuilder()
            .setFormat(format.toProto());
        for (VariableAssignment arg : args) {
            out.addArgs(arg.toProto());
        }
        return out;
    }

    public void initFrom(Message proto) {
        FormatStringPb p = (FormatStringPb) proto;
        format = VariableAssignment.fromProto(p.getFormat());
        for (VariableAssignmentPb arg : p.getArgsList()) {
            args.add(VariableAssignment.fromProto(arg));
        }
    }
}
