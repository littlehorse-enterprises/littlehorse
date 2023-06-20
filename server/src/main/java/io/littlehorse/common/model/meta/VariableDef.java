package io.littlehorse.common.model.meta;

import com.google.protobuf.Message;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.jlib.common.proto.VariableDefPb;
import io.littlehorse.jlib.common.proto.VariableTypePb;

public class VariableDef extends LHSerializable<VariableDefPb> {

    public VariableTypePb type;
    public String name;

    public ThreadSpec threadSpec;

    public Class<VariableDefPb> getProtoBaseClass() {
        return VariableDefPb.class;
    }

    public void initFrom(Message proto) {
        VariableDefPb p = (VariableDefPb) proto;
        type = p.getType();
        name = p.getName();
    }

    public VariableDefPb.Builder toProto() {
        VariableDefPb.Builder out = VariableDefPb
            .newBuilder()
            .setType(type)
            .setName(name);

        return out;
    }

    public static VariableDef fromProto(VariableDefPb proto) {
        VariableDef o = new VariableDef();
        o.initFrom(proto);
        return o;
    }
}
