package io.littlehorse.common.model.wfrun;

import com.google.protobuf.Message;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.sdk.common.proto.VarNameAndVal;

public class VarNameAndValModel extends LHSerializable<VarNameAndVal> {

    String varName;
    VariableValueModel value;

    public VarNameAndValModel() {}

    public VarNameAndValModel(String name, VariableValueModel val) {
        this.varName = name;
        this.value = val;
    }

    public Class<VarNameAndVal> getProtoBaseClass() {
        return VarNameAndVal.class;
    }

    public VarNameAndVal.Builder toProto() {
        VarNameAndVal.Builder out = VarNameAndVal.newBuilder();
        out.setVarName(varName);
        out.setValue(value.toProto());

        return out;
    }

    public void initFrom(Message proto) {
        VarNameAndVal p = (VarNameAndVal) proto;
        varName = p.getVarName();
        value = VariableValueModel.fromProto(p.getValue());
    }
}
