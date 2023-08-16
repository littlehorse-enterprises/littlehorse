package io.littlehorse.common.model.wfrun;

import com.google.protobuf.Message;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.sdk.common.proto.VarNameAndValPb;

public class VarNameAndVal extends LHSerializable<VarNameAndValPb> {

    String varName;
    VariableValueModel value;

    public VarNameAndVal() {}

    public VarNameAndVal(String name, VariableValueModel val) {
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

    public void initFrom(Message proto) {
        VarNameAndValPb p = (VarNameAndValPb) proto;
        varName = p.getVarName();
        value = VariableValueModel.fromProto(p.getValue());
    }
}
