package io.littlehorse.server.streams.lhinternalscan.publicrequests;

import com.google.protobuf.Message;

import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.getable.core.variable.VariableValueModel;
import io.littlehorse.sdk.common.proto.VariableMatch;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import lombok.Getter;

@Getter
public class VariableMatchModel extends LHSerializable<VariableMatch> {
    private VariableValueModel value;
    private String varName;

    @Override
    public Class<VariableMatch> getProtoBaseClass() {
        return VariableMatch.class;
    }

    @Override
    public VariableMatch.Builder toProto() {
        VariableMatch.Builder out = VariableMatch.newBuilder().setValue(value.toProto()).setVarName(varName);
        return out;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext ctx) {
        VariableMatch p = (VariableMatch) proto;
        varName = p.getVarName();
        value = VariableValueModel.fromProto(p.getValue(), ctx);
    }
}
