package io.littlehorse.common.model.getable.core.taskrun;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.getable.core.variable.VariableValueModel;
import io.littlehorse.sdk.common.proto.VarNameAndVal;
import io.littlehorse.server.streams.topology.core.ExecutionContext;

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

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        VarNameAndVal p = (VarNameAndVal) proto;
        varName = p.getVarName();
        value = VariableValueModel.fromProto(p.getValue(), context);
    }
}
