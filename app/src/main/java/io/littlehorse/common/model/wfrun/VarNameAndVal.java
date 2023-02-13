package io.littlehorse.common.model.wfrun;

import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.jlib.common.proto.VarNameAndValPb;
import io.littlehorse.jlib.common.proto.VarNameAndValPbOrBuilder;

public class VarNameAndVal extends LHSerializable<VarNameAndValPb> {

    String varName;
    VariableValue value;

    public VarNameAndVal() {}

    public VarNameAndVal(String name, VariableValue val) {
        this.varName = name;
        this.value = val;
    }

    public Class<VarNameAndValPb> getProtoBaseClass() {
        return VarNameAndValPb.class;
    }

    public VarNameAndValPb.Builder toProto() {
        VarNameAndValPb.Builder out = VarNameAndValPb.newBuilder();
        out.setVarName(varName);
        out.setValue(value.toProto());

        return out;
    }

    public void initFrom(MessageOrBuilder proto) {
        VarNameAndValPbOrBuilder p = (VarNameAndValPbOrBuilder) proto;
        varName = p.getVarName();
        value = VariableValue.fromProto(p.getValueOrBuilder());
    }
}
