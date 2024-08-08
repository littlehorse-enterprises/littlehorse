package io.littlehorse.common.model.getable.core.taskrun;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.getable.core.variable.VariableValueModel;
import io.littlehorse.sdk.common.proto.VarNameAndVal;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.topology.core.RequestExecutionContext;
import lombok.Getter;

public class VarNameAndValModel extends LHSerializable<VarNameAndVal> {

    @Getter
    String varName;

    VariableValueModel value;

    @Getter
    boolean masked;

    ExecutionContext context;

    public VarNameAndValModel() {}

    public VarNameAndValModel(String name, VariableValueModel val, boolean masked) {
        this.varName = name;
        this.value = val;
        this.masked = masked;
    }

    public Class<VarNameAndVal> getProtoBaseClass() {
        return VarNameAndVal.class;
    }

    public VarNameAndVal.Builder toProto() {
        VarNameAndVal.Builder out = VarNameAndVal.newBuilder();
        out.setVarName(varName);
        out.setValue(value.toProto());
        if (masked && context instanceof RequestExecutionContext) {
            out.setValue(new VariableValueModel("****").toProto());
        }
        out.setMasked(masked);

        return out;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        VarNameAndVal p = (VarNameAndVal) proto;
        varName = p.getVarName();
        value = VariableValueModel.fromProto(p.getValue(), context);
        masked = p.getMasked();
        this.context = context;
    }
}
